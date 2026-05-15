#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────
# 측정 시나리오별 toxic 적용/해제 스크립트
#
# 사용:
#   ./load-test/toxiproxy/scenarios.sh s1     # 정상
#   ./load-test/toxiproxy/scenarios.sh s2     # 일시 장애 (latency + 일부 5xx)
#   ./load-test/toxiproxy/scenarios.sh s3     # 완전 장애 (모든 연결 끊김)
#   ./load-test/toxiproxy/scenarios.sh reset  # 모든 toxic 제거 (=s1 동일)
#
# 주의:
#   - timeout toxic 은 일정 시간 후 connection drop. AI 서버의 5xx를 흉내내려면
#     실제로는 WireMock 등이 필요하지만 baseline 측정에는 connection drop 으로 충분.
# ─────────────────────────────────────────────────────────────────────
set -euo pipefail

TOXIPROXY="${TOXIPROXY_URL:-http://localhost:8474}"
PROXY_NAME="ai-server"

# 모든 toxic 제거
clear_toxics() {
  local toxics
  toxics=$(curl -fsS "${TOXIPROXY}/proxies/${PROXY_NAME}/toxics" | python3 -c "import sys, json; print(' '.join(t['name'] for t in json.load(sys.stdin)))" 2>/dev/null || echo "")
  for t in $toxics; do
    curl -fsS -X DELETE "${TOXIPROXY}/proxies/${PROXY_NAME}/toxics/${t}" >/dev/null
    echo "  - removed toxic: ${t}"
  done
}

# 프록시 enabled 상태 토글
# enabled 만 보내면 Toxiproxy 가 listen/upstream 을 빈 값으로 덮어써서 hang 발생
# → 현재 프록시 설정 전체를 GET 한 뒤 enabled 만 교체해서 재 POST
set_enabled() {
  local enabled="$1"
  local config
  config=$(curl -fsS --max-time 5 "${TOXIPROXY}/proxies/${PROXY_NAME}")
  echo "${config}" | python3 -c "
import sys, json
p = json.load(sys.stdin)
p['enabled'] = ('${enabled}' == 'true')
print(json.dumps(p))
" | curl -fsS --max-time 10 -X POST "${TOXIPROXY}/proxies/${PROXY_NAME}" \
    -H "Content-Type: application/json" \
    -d @- >/dev/null
}

case "${1:-}" in
  s1|reset)
    echo "[S1] Normal — 모든 toxic 제거, proxy enabled"
    clear_toxics
    set_enabled true
    ;;
  s2)
    echo "[S2] 일시 장애 — latency 2000ms±500ms (100%) + timeout toxic 30%"
    clear_toxics
    set_enabled true

    # 모든 응답에 2초 ± 500ms 지연 (downstream 방향 = 응답 경로)
    curl -fsS -X POST "${TOXIPROXY}/proxies/${PROXY_NAME}/toxics" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "latency_down",
        "type": "latency",
        "stream": "downstream",
        "toxicity": 1.0,
        "attributes": {"latency": 2000, "jitter": 500}
      }' >/dev/null

    # 30% 의 응답에 대해 일정 시간 후 connection drop 처럼 동작
    curl -fsS -X POST "${TOXIPROXY}/proxies/${PROXY_NAME}/toxics" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "timeout_down",
        "type": "timeout",
        "stream": "downstream",
        "toxicity": 0.3,
        "attributes": {"timeout": 0}
      }' >/dev/null
    ;;
  s3)
    echo "[S3] 완전 장애 — proxy disabled (모든 연결 즉시 끊김)"
    clear_toxics
    set_enabled false
    ;;
  *)
    echo "사용: $0 {s1|s2|s3|reset}"
    exit 1
    ;;
esac

echo "[done] 현재 toxics:"
curl -fsS "${TOXIPROXY}/proxies/${PROXY_NAME}/toxics" | python3 -m json.tool
