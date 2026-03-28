import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");

/**
 * Ortam: docker-compose sonrasi dispatcher http://localhost:8080
 *
 * Ornekler:
 *   k6 run --vus 50 --duration 30s load-tests/k6-dispatcher.js
 *   k6 run --vus 200 --duration 1m -e BASE_URL=http://localhost:8080 load-tests/k6-dispatcher.js
 */
export const options = {
  stages: [
    { duration: "10s", target: 50 },
    { duration: "30s", target: 100 },
    { duration: "10s", target: 200 },
    { duration: "30s", target: 200 },
    { duration: "10s", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<3000"],
    errors: ["rate<0.1"],
  },
};

const BASE = __ENV.BASE_URL || "http://localhost:8080";

export default function () {
  const r1 = http.get(`${BASE}/auth/test`);
  const ok1 = check(r1, { "auth test 2xx": (r) => r.status >= 200 && r.status < 300 });
  errorRate.add(!ok1);
  sleep(0.15);
}
