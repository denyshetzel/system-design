import http from "k6/http";
import { check } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8081";
const CREATE_PATH = __ENV.CREATE_PATH || "/shortening/api/shortener";
const FULL_URL = `${BASE_URL}${CREATE_PATH}`;

export const options = {
  vus: Number(__ENV.VUS || 1),
  iterations: Number(__ENV.ITERATIONS || 20),
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<300"],
    checks: ["rate>0.99"],
  },
};

export default function () {
  const payload = JSON.stringify({
    originalUrl: `https://example.com/smoke/${__VU}-${__ITER}-${Date.now()}`,
    expirationSeconds: 60,
  });

  const res = http.post(FULL_URL, payload, {
    headers: { "Content-Type": "application/json" },
  });

  check(res, {
    "status is 201": (r) => r.status === 201,
  });
}
