#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────
# 재측정 실행 스크립트: 시나리오 적용 → k6 → 비동기 작업 드레인 → 결과 수집
#
# 사용:
#   MYSQL_PWD=... ./load-test/run.sh <before|after> <s1|s2|s3|s4>
#
# 시나리오 (fake-ai + toxiproxy)
#   s1  정상               fail 0%,  latency 0
#   s2  일시 장애          fail 30% (요청 단위 503)          → Retry 효과
#   s3  완전 장애          fail 100% (모든 요청 503)         → CircuitBreaker 효과
#   s4  AI 응답 지연       toxiproxy latency 2000±500ms      → 분석 워커 풀 병목 재현
#
# 핵심 지표
#   - 분석 완료율   = COMPLETED / k6 전체 요청 수   (DB analysis 테이블 기준)
#   - AI 서버 도달 호출 수 (fake-ai /__stats) — 장애 서버로 향하는 부하
#   - resilience4j 카운터 증분 (retry / circuitbreaker not_permitted)
# ─────────────────────────────────────────────────────────────────────
set -euo pipefail

LABEL="${1:?before|after}"
SCENARIO="${2:?s1|s2|s3|s4}"

APP="${APP_URL:-http://localhost:8080}"
FAKE_AI="${FAKE_AI_URL:-http://localhost:8000}"
TOXIPROXY="${TOXIPROXY_URL:-http://localhost:8474}"
RPS="${RPS:-5}"
DURATION="${DURATION:-3m}"
DRAIN_TIMEOUT_SEC="${DRAIN_TIMEOUT_SEC:-180}"
MYSQL_ARGS=(-h "${MYSQL_HOST:-127.0.0.1}" -u "${MYSQL_USER:-dongkyun}" "${MYSQL_DB:-acci}" -N -B)
: "${MYSQL_PWD:?MYSQL_PWD 환경변수 필요}"
export MYSQL_PWD

ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="${ROOT}/results/rerun/${LABEL}-${SCENARIO}"
mkdir -p "${OUT}"

chaos() { curl -fsS -X POST "${FAKE_AI}/__chaos" -H 'Content-Type: application/json' -d "$1" >/dev/null; }
metrics_snapshot() { curl -fsS "${APP}/actuator/prometheus" | grep -E '^resilience4j_(retry_calls_total|circuitbreaker_not_permitted_calls_total|circuitbreaker_calls_seconds_count)' || true; }

# 1. 시나리오 적용 ------------------------------------------------------
"${ROOT}/toxiproxy/scenarios.sh" reset >/dev/null
case "${SCENARIO}" in
  s1) chaos '{"fail_rate": 0.0, "latency_ms": 0}' ;;
  s2) chaos '{"fail_rate": 0.3, "latency_ms": 0}' ;;
  s3) chaos '{"fail_rate": 1.0, "latency_ms": 0}' ;;
  s4) chaos '{"fail_rate": 0.0, "latency_ms": 0}'
      curl -fsS -X POST "${TOXIPROXY}/proxies/ai-server/toxics" -H 'Content-Type: application/json' \
        -d '{"name":"latency_down","type":"latency","stream":"downstream","toxicity":1.0,"attributes":{"latency":2000,"jitter":500}}' >/dev/null ;;
  *) echo "unknown scenario: ${SCENARIO}"; exit 1 ;;
esac
curl -fsS -X POST "${FAKE_AI}/__stats/reset" >/dev/null
metrics_snapshot > "${OUT}/metrics-before.txt"

START="$(date '+%Y-%m-%d %H:%M:%S')"
echo "[${LABEL}-${SCENARIO}] start=${START} rps=${RPS} duration=${DURATION}"

# 2. 부하 ---------------------------------------------------------------
BASE_URL="${APP}" RPS="${RPS}" DURATION="${DURATION}" \
  k6 run --quiet --summary-export="${OUT}/k6-summary.json" "${ROOT}/k6/baseline.js" | tee "${OUT}/k6.log"

# 3. 비동기 분석 작업이 모두 끝날 때까지 대기 ---------------------------
WHERE="created_at >= '${START}'"
for ((i = 0; i < DRAIN_TIMEOUT_SEC; i += 5)); do
  processing=$(mysql "${MYSQL_ARGS[@]}" -e "SELECT COUNT(*) FROM analysis WHERE ${WHERE} AND accident_status = 'PROCESSING'")
  [[ "${processing}" == "0" ]] && break
  echo "  draining... PROCESSING=${processing}"
  sleep 5
done

# 4. 수집 ---------------------------------------------------------------
mysql "${MYSQL_ARGS[@]}" -e "SELECT accident_status, COUNT(*) FROM analysis WHERE ${WHERE} GROUP BY accident_status" > "${OUT}/db-status.tsv"
curl -fsS "${FAKE_AI}/__stats" > "${OUT}/fake-ai-stats.json"
metrics_snapshot > "${OUT}/metrics-after.txt"
"${ROOT}/toxiproxy/scenarios.sh" reset >/dev/null
chaos '{"fail_rate": 0.0, "latency_ms": 0}'

python3 - "${OUT}" "${LABEL}-${SCENARIO}" <<'EOF'
import json, sys, pathlib, re
out, name = pathlib.Path(sys.argv[1]), sys.argv[2]

k6 = json.loads((out / "k6-summary.json").read_text())["metrics"]
total = int(k6["http_reqs"]["count"])
accepted = int(k6["analysis_accepted_rate"]["passes"])
p99 = k6["http_req_duration"]["p(99)"]

db = dict(line.split("\t") for line in (out / "db-status.tsv").read_text().split("\n") if line)
completed, failed = int(db.get("COMPLETED", 0)), int(db.get("FAILED", 0))

ai = json.loads((out / "fake-ai-stats.json").read_text())["stats"]
ai_calls = sum(sum(v.values()) for v in ai.values())

def counters(f):
    res = {}
    for line in (out / f).read_text().splitlines():
        m = re.match(r"(\S+)\s+([\d.eE+-]+)$", line)
        if m: res[m.group(1)] = float(m.group(2))
    return res
before, after = counters("metrics-before.txt"), counters("metrics-after.txt")
delta = {k: after[k] - before.get(k, 0) for k in after if after[k] - before.get(k, 0) > 0}

summary = {
    "run": name,
    "k6_total": total,
    "k6_202": accepted,
    "k6_p99_ms": round(p99, 1),
    "db_completed": completed,
    "db_failed": failed,
    "completion_rate": round(completed / total, 3) if total else None,
    "ai_server_calls": ai_calls,
    "ai_server_calls_per_job": round(ai_calls / accepted, 2) if accepted else None,
    "resilience4j_delta": delta,
}
(out / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2))
print(json.dumps(summary, ensure_ascii=False, indent=2))
EOF
