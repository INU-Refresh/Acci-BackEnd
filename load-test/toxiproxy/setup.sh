#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────
# Toxiproxy 초기 설정 스크립트
# - 기존 ai-server 프록시가 있으면 삭제 후 재등록 (idempotent)
# - upstream 기본값은 같은 docker network 의 wiremock 컨테이너
#   (재현 가능한 응답 보장 위해 실제 AI 서버 대신 wiremock 사용)
#
# 사용 예:
#   ./load-test/toxiproxy/setup.sh
#   AI_UPSTREAM=host.docker.internal:8000 ./load-test/toxiproxy/setup.sh   # 실제 AI 서버
# ─────────────────────────────────────────────────────────────────────
set -euo pipefail

TOXIPROXY="${TOXIPROXY_URL:-http://localhost:8474}"
# 기본 upstream: docker compose 네트워크 안의 wiremock:8080
AI_UPSTREAM="${AI_UPSTREAM:-wiremock:8080}"
PROXY_NAME="ai-server"
LISTEN="0.0.0.0:8666"

echo "[setup] Toxiproxy=${TOXIPROXY}, upstream=${AI_UPSTREAM}"

# 기존 프록시 제거 (없으면 무시)
curl -fsS -X DELETE "${TOXIPROXY}/proxies/${PROXY_NAME}" >/dev/null 2>&1 || true

# 새 프록시 등록
curl -fsS -X POST "${TOXIPROXY}/proxies" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"${PROXY_NAME}\",
    \"listen\": \"${LISTEN}\",
    \"upstream\": \"${AI_UPSTREAM}\",
    \"enabled\": true
  }" >/dev/null

echo "[setup] '${PROXY_NAME}' 프록시 등록 완료 (listen=${LISTEN})"
echo "[setup] 앱에서 AI_SERVER_URL=http://toxiproxy:8666 (컨테이너) 또는 http://localhost:8666 (호스트) 로 설정"
