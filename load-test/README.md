# Load Test (AI 서버 회복 탄력성 측정)

Resilience4j 적용 **전/후** 같은 조건으로 측정해 비교하기 위한 부하 테스트 도구.
운영 코드와 격리된 폴더이며, 평상시 빌드/배포에는 포함되지 않음.

---

## 데이터 흐름

```
k6 ──► app:8080 ──► toxiproxy:8666 ──► wiremock:8080
                    (장애 주입)         (가짜 AI 서버)
```

- 실제 AI 서버 대신 **WireMock**을 두어 응답 시간 변동을 제거 (baseline 노이즈 제로화)
- AI 서버 호출 경로에만 **Toxiproxy**로 인위 장애(latency / connection drop) 주입

---

## 디렉토리 구조

```
load-test/
├── README.md                    # 이 문서
├── k6/
│   └── baseline.js              # k6 부하 스크립트 (POST /api/v1/analyses)
├── toxiproxy/
│   ├── setup.sh                 # AI 서버 프록시 등록 (upstream=wiremock:8080)
│   └── scenarios.sh             # S1/S2/S3 시나리오 toxic 적용/해제
├── wiremock/
│   └── mappings/                # AI 서버 stub 응답 정의 (analyze/status/result)
└── results/                     # 측정 결과 저장
```

---

## 사전 준비

1. **Toxiproxy + WireMock 기동**
   ```bash
   docker compose --profile load-test up -d toxiproxy wiremock
   ```

2. **AI 서버 프록시 등록**
   ```bash
   # 기본값: wiremock 으로 라우팅
   ./load-test/toxiproxy/setup.sh
   ```

3. **앱이 Toxiproxy 경유하도록 설정**
   - Docker 컨테이너로 앱 실행: `AI_SERVER_URL=http://toxiproxy:8666`
   - 호스트에서 직접 앱 실행: `AI_SERVER_URL=http://localhost:8666`

4. **JWT 토큰 발급**
   `/api/v1/analyses`는 인증 필요 엔드포인트.
   웹 로그인 후 브라우저 쿠키의 `accessToken` 값 또는 OAuth 콜백 응답의 access token을 복사.

5. **k6 설치** (이미 설치된 경우 생략)
   ```bash
   brew install k6
   ```

---

## 측정 시나리오

| 시나리오 | AI 서버 상태 (Toxiproxy 설정) | 목적 |
|---|---|---|
| **S1. Normal** | 정상 (toxic 없음) → wiremock 즉시 응답 | 기준선 |
| **S2. 일시 장애** | latency +2000ms ± 500 (전체) + timeout 30% | Retry/Backoff 효과 검증 |
| **S3. 완전 장애** | proxy disabled (모든 연결 즉시 끊김) | CircuitBreaker / Bulkhead 효과 검증 |

각 시나리오는 `toxiproxy/scenarios.sh` 로 적용/해제.

---

## WireMock stub 동작

| 엔드포인트 | 응답 |
|---|---|
| `POST /api/v1/analyze` | 200 + `{ job_id: <UUID>, status: "queued", message: "accepted" }` |
| `GET /api/v1/status/{jobId}` | 200 + `{ job_id, status: "completed" }` (즉시 완료 → 폴링 1회로 종료) |
| `GET /api/v1/result/{jobId}` | 200 + 고정된 사고 분류 결과 |

> baseline 측정 목적상 폴링 루프는 빠르게 종료시켜 **"AI 호출 시점의 회복력"** 자체에 집중.

---

## 측정 실행 (Before / After 동일하게)

```bash
# 1. 시나리오 적용
./load-test/toxiproxy/scenarios.sh s1   # 또는 s2 / s3

# 2. k6 실행 (5분, 5 RPS 예시)
JWT_TOKEN="<발급받은 토큰>" \
BASE_URL=http://localhost:8080 \
RPS=5 DURATION=5m \
k6 run --summary-export=load-test/results/before-s1.json load-test/k6/baseline.js

# 3. 시나리오 해제
./load-test/toxiproxy/scenarios.sh reset
```

Resilience4j 적용 후 동일 명령으로 다시 실행하고 `before-` / `after-` 접두사로 결과 비교.

---

## Grafana 에서 함께 봐야 할 메트릭

- `http_server_requests_seconds_count{uri=~".*analyses.*"}` (status 별)
- `http_server_requests_seconds{quantile="0.95"}`
- `tomcat_threads_busy_threads`
- `jvm_memory_used_bytes`
- 적용 후엔 `resilience4j_circuitbreaker_state`, `resilience4j_retry_calls`
