"""
부하 테스트용 가짜 AI 서버 (요청 단위 확률적 장애 주입)

WireMock / Toxiproxy 로는 "요청마다 30% 확률로 503" 을 만들 수 없어서 별도 구현.
  - Toxiproxy toxicity 는 커넥션 단위로 적용 → WebClient 커넥션 풀 재사용 시 실제 실패율이 왜곡됨
  - WireMock 은 확률 기반 status 응답을 지원하지 않음

엔드포인트 (실제 AI 서버와 동일)
  POST /api/v1/analyze            → { job_id, status: queued }
  GET  /api/v1/status/{jobId}     → { job_id, status: completed }  (폴링 1회로 종료)
  GET  /api/v1/result/{jobId}     → 고정된 사고 분류 결과

관리 엔드포인트
  POST /__chaos        {"fail_rate": 0.3, "latency_ms": 0}   장애 설정 변경
  GET  /__stats        엔드포인트별 200 / 503 응답 수
  POST /__stats/reset  카운터 초기화
"""
import json
import random
import threading
import time
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

_lock = threading.Lock()
_config = {"fail_rate": 0.0, "latency_ms": 0}
_stats = {}

RESULT_BODY = {
    "accident_type": 7,
    "vehicle_A_fault": 60,
    "vehicle_B_fault": 40,
    "classification_info": {
        "place": "교차로",
        "situation": "신호등 있는 교차로 직진 충돌",
        "vehicle_a": "직진",
        "vehicle_b": "직진",
    },
}


def _count(endpoint, status):
    with _lock:
        per = _stats.setdefault(endpoint, {})
        per[str(status)] = per.get(str(status), 0) + 1


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"  # keep-alive 유지 (WebClient 커넥션 풀과 동일 조건)

    def log_message(self, *args):
        pass

    # ── 공통 ──────────────────────────────────────────────
    def _drain_body(self):
        # multipart 업로드는 chunked 로 올 수 있으므로 두 방식 모두 처리
        if self.headers.get("Transfer-Encoding", "").lower() == "chunked":
            while True:
                size = int(self.rfile.readline().strip() or b"0", 16)
                if size == 0:
                    self.rfile.readline()
                    break
                self.rfile.read(size)
                self.rfile.readline()
            return b""
        length = int(self.headers.get("Content-Length") or 0)
        return self.rfile.read(length) if length else b""

    def _send(self, status, body):
        data = json.dumps(body, ensure_ascii=False).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def _chaos(self, endpoint, ok_body):
        with _lock:
            fail_rate, latency_ms = _config["fail_rate"], _config["latency_ms"]
        if latency_ms:
            time.sleep(latency_ms / 1000)
        if random.random() < fail_rate:
            _count(endpoint, 503)
            self._send(503, {"detail": "injected failure"})
        else:
            _count(endpoint, 200)
            self._send(200, ok_body)

    # ── 라우팅 ────────────────────────────────────────────
    def do_POST(self):
        raw = self._drain_body()
        if self.path == "/api/v1/analyze":
            self._chaos("analyze", {"job_id": str(uuid.uuid4()), "status": "queued", "message": "accepted"})
        elif self.path == "/__chaos":
            body = json.loads(raw or b"{}")
            with _lock:
                _config.update({k: body[k] for k in ("fail_rate", "latency_ms") if k in body})
                snapshot = dict(_config)
            self._send(200, snapshot)
        elif self.path == "/__stats/reset":
            with _lock:
                _stats.clear()
            self._send(200, {})
        else:
            self._send(404, {})

    def do_GET(self):
        parts = self.path.strip("/").split("/")
        if self.path.startswith("/api/v1/status/"):
            self._chaos("status", {"job_id": parts[-1], "status": "completed"})
        elif self.path.startswith("/api/v1/result/"):
            self._chaos("result", {"job_id": parts[-1], **RESULT_BODY})
        elif self.path == "/__stats":
            with _lock:
                self._send(200, {"config": dict(_config), "stats": _stats})
        else:
            self._send(404, {})


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 8000), Handler).serve_forever()
