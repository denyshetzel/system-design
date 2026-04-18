import http from "k6/http";
import { check } from "k6";
import { Counter, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8081";
const CREATE_PATH = __ENV.CREATE_PATH || "/shortening/api/shortener";
const FULL_URL = `${BASE_URL}${CREATE_PATH}`;

const RATE = Number(__ENV.RATE || 30);
const DURATION = __ENV.DURATION || "2m";
const PRE_ALLOCATED_VUS = Number(__ENV.PRE_ALLOCATED_VUS || 20);
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const P95_MS = Number(__ENV.P95_MS || 400);
const P99_MS = Number(__ENV.P99_MS || 800);
const ERROR_RATE = Number(__ENV.ERROR_RATE || 0.01);

const createUrlDuration = new Trend("create_short_url_duration", true);
const createUrlFailures = new Counter("create_short_url_failures");

export const options = {
  scenarios: {
    create_short_urls: {
      executor: "constant-arrival-rate",
      rate: RATE,
      timeUnit: "1s",
      duration: DURATION,
      preAllocatedVUs: PRE_ALLOCATED_VUS,
      maxVUs: MAX_VUS,
    },
  },
  thresholds: {
    http_req_failed: [`rate<${ERROR_RATE}`],
    http_req_duration: [`p(95)<${P95_MS}`, `p(99)<${P99_MS}`],
    create_short_url_duration: [`p(95)<${P95_MS}`, `p(99)<${P99_MS}`],
    checks: ["rate>0.99"],
  },
  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

export default function () {
  const uniqueToken = `${__VU}-${__ITER}-${Date.now()}`;
  // Rotate client IP to avoid hitting per-IP rate limit during load tests.
  const ipOctetA = (__VU % 250) + 1;
  const ipOctetB = (__ITER % 250) + 1;
  const ipOctetC = (Math.floor(__ITER / 250) % 250) + 1;
  const payload = JSON.stringify({
    originalUrl: `https://example.com/perf/${uniqueToken}`,
    expirationSeconds: 86400,
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      "X-Forwarded-For": `10.${ipOctetA}.${ipOctetB}.${ipOctetC}`,
    },
    tags: {
      endpoint: "create_short_url",
    },
  };

  const res = http.post(FULL_URL, payload, params);
  createUrlDuration.add(res.timings.duration);

  const ok = check(res, {
    "status is 201": (r) => r.status === 201,
    "response has shortCode": (r) => {
      try {
        const body = JSON.parse(r.body);
        return !!body.shortCode;
      } catch (e) {
        return false;
      }
    },
  });

  if (!ok) {
    createUrlFailures.add(1);
  }
}
