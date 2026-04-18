import http from "k6/http";
import { Counter, Rate, Trend } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://api:8080";
const CREATE_PATH = __ENV.CREATE_PATH || "/shortening/api/shortener";
const FULL_URL = `${BASE_URL}${CREATE_PATH}`;

const RATE = Number(__ENV.RATE || 100);
const DURATION = __ENV.DURATION || "30s";
const PRE_ALLOCATED_VUS = Number(__ENV.PRE_ALLOCATED_VUS || 100);
const MAX_VUS = Number(__ENV.MAX_VUS || 1000);

const timeoutErrors = new Counter("timeout_errors");
const non201Errors = new Counter("non_201_errors");
const timeoutRate = new Rate("timeout_rate");
const createDuration = new Trend("create_duration", true);

export const options = {
  discardResponseBodies: true,
  scenarios: {
    stress_create_urls: {
      executor: "constant-arrival-rate",
      rate: RATE,
      timeUnit: "1s",
      duration: DURATION,
      preAllocatedVUs: PRE_ALLOCATED_VUS,
      maxVUs: MAX_VUS,
    },
  },
  summaryTrendStats: ["avg", "med", "p(90)", "p(95)", "p(99)", "max"],
};

export default function () {
  const uniqueToken = `${__VU}-${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    originalUrl: `https://example.com/stress/${uniqueToken}`,
    expirationSeconds: 86400,
  });

  // Vary X-Forwarded-For to avoid single-IP rate-limit bottleneck during stress tests.
  const ipOctetA = (__VU % 250) + 1;
  const ipOctetB = (__ITER % 250) + 1;
  const ipOctetC = (Math.floor(__ITER / 250) % 250) + 1;

  const res = http.post(FULL_URL, payload, {
    headers: {
      "Content-Type": "application/json",
      "X-Forwarded-For": `10.${ipOctetA}.${ipOctetB}.${ipOctetC}`,
    },
  });

  createDuration.add(res.timings.duration);

  const isTimeout = !!res.error && String(res.error).toLowerCase().includes("timeout");
  timeoutRate.add(isTimeout);
  if (isTimeout) {
    timeoutErrors.add(1);
  }

  if (res.status !== 201) {
    non201Errors.add(1);
  }
}
