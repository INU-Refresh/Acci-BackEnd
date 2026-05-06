// AI 서버 회복 탄력성 베이스라인 측정용 k6 스크립트
//
// 대상: POST /api/v1/analyses (영상 업로드 → 비동기 AI 분석 트리거)
// 인증: JWT_TOKEN 환경변수로 Bearer 토큰 주입
// 영상: 더미 바이트 (init 1회 생성, 크기는 VIDEO_SIZE_BYTES 환경변수로 조정)
//
// 환경변수:
//   BASE_URL           기본 http://localhost:8080
//   JWT_TOKEN
//   RPS                초당 요청 수 (기본 5)
//   DURATION           시나리오 지속 시간 (기본 2m)
//   VIDEO_SIZE_BYTES   더미 영상 크기 (기본 102400 = 100KB)
//
// 실행:
//   JWT_TOKEN=... k6 run --summary-export=load-test/results/before-s1.json \
//                        load-test/k6/baseline.js

import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';

// 설정
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const JWT_TOKEN = __ENV.JWT_TOKEN || '';
const VIDEO_SIZE_BYTES = parseInt(__ENV.VIDEO_SIZE_BYTES || '102400', 10);

// 더미 영상 바이트는 init 컨텍스트에서 1회만 생성 (VU 마다 재할당 금지)
const VIDEO_BYTES = (function () {
    const buf = new Uint8Array(VIDEO_SIZE_BYTES);
    for (let i = 0; i < buf.length; i++) buf[i] = i & 0xff;
    return buf.buffer;
})();

// 커스텀 메트릭
// 202 ACCEPTED 응답률 (Resilience4j 적용 전후 비교의 핵심 KPI)
const acceptedRate = new Rate('analysis_accepted_rate');

// k6 옵션
export const options = {
    scenarios: {
        // constant-arrival-rate: 외부 부하원처럼 일정 RPS 유지
        // (응답이 느려져도 요청 시점 자체는 늦추지 않음 → 백엔드 누적 부하 측정)
        analysis_upload: {
            executor: 'constant-arrival-rate',
            rate: parseInt(__ENV.RPS || '5', 10),
            timeUnit: '1s',
            duration: __ENV.DURATION || '2m',
            preAllocatedVUs: 20,
            maxVUs: 100,
        },
    },
    thresholds: {
        // 측정 자체가 목적이므로 임계는 느슨하게 두고 결과 데이터에 집중
        'http_req_duration': ['p(95)<60000'],
        'http_req_failed': ['rate<1.0'],
    },
    // 결과를 태그별로 분리하기 쉽게 sample 보존
    summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// 시나리오 함수
export default function () {
    const headers = {};
    if (JWT_TOKEN) {
        headers['Authorization'] = `Bearer ${JWT_TOKEN}`;
    }

    // multipart/form-data 자동 인코딩 (k6 가 'video/mp4' 파트로 전송)
    const formData = {
        video: http.file(VIDEO_BYTES, 'sample.mp4', 'video/mp4'),
    };

    const res = http.post(`${BASE_URL}/api/v1/analyses`, formData, {
        headers,
        timeout: '60s',
        tags: { endpoint: 'analyses_upload' },
    });

    // 202 ACCEPTED 가 정상 (Spring 컨트롤러 반환 코드)
    const isAccepted = res.status === 202;
    acceptedRate.add(isAccepted);

    check(res, {
        'status is 202': (r) => r.status === 202,
        'no 5xx': (r) => r.status < 500,
    });
}

// setup/teardown 훅
export function setup() {
    if (!JWT_TOKEN) {
        console.warn('[!] JWT_TOKEN 미설정 → 401 가 대량 발생할 수 있음');
    }
    console.log(`[setup] BASE_URL=${BASE_URL}, RPS=${__ENV.RPS || 5}, DURATION=${__ENV.DURATION || '2m'}, video=${VIDEO_SIZE_BYTES}B`);
}
