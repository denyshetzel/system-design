# Performance Tests (k6)

This directory provides a professional and reusable load-testing baseline for non-functional requirements (NFR), focused on latency, throughput, and error rate.

## Available Scenarios

- `k6/smoke-api.js`: quick sanity check to confirm API health.
- `k6/load-api.js`: continuous load using `constant-arrival-rate` and SLA thresholds.

## Prerequisites

- Docker running.
- API available at `http://localhost:8081` (or set `BASE_URL`).

## Local Execution with Docker

### 1) Smoke test

```powershell
docker run --rm -i `
  -e BASE_URL=http://host.docker.internal:8081 `
  -v ${PWD}/perf/k6:/scripts `
  grafana/k6 run /scripts/smoke-api.js
```

### 2) Load test with SLA

```powershell
docker run --rm -i `
  -e BASE_URL=http://host.docker.internal:8081 `
  -e RATE=50 `
  -e DURATION=5m `
  -e PRE_ALLOCATED_VUS=50 `
  -e MAX_VUS=300 `
  -e P95_MS=400 `
  -e P99_MS=800 `
  -e ERROR_RATE=0.01 `
  -v ${PWD}/perf/k6:/scripts `
  grafana/k6 run /scripts/load-api.js
```

## NFR Interpretation

- `http_req_duration p(95)` below `P95_MS`.
- `http_req_duration p(99)` below `P99_MS`.
- `http_req_failed` below `ERROR_RATE`.
- `checks` above `99%`.

If any threshold fails, the test should be considered failed for the defined SLA.

## Continuous Usage (Recommended)

- Run `smoke-api.js` on pull requests.
- Run `load-api.js` on nightly/release pipelines.
- Version SLA thresholds together with code for traceability.

## Front-end

For web front-end testing, use together:

- `k6` for HTTP/API traffic at scale.
- `Lighthouse CI` for Web Vitals (LCP, CLS, INP), render time, and performance budgets.

This combo covers both backend performance and end-user experience with industry-standard tooling.

## JIT vs Native Benchmark

Start both services before benchmarking:

```powershell
docker compose up -d --build api api-native
```

Run the comparison:

```powershell
.\perf\benchmark-jit-vs-native.ps1
```

By default, the script uses the internal compose network:
- JIT: `http://api:8080`
- Native: `http://api-native:8080`
- Network: `url-shortering_backend`

To use `host.docker.internal` (fallback):

```powershell
.\perf\benchmark-jit-vs-native.ps1 `
  -UseComposeNetwork:$false `
  -JitBaseUrl http://host.docker.internal:8081 `
  -NativeBaseUrl http://host.docker.internal:8082
```

With custom load settings:

```powershell
.\perf\benchmark-jit-vs-native.ps1 -Rate 80 -Duration 5m -MaxVus 500
```

The script generates a summary with `RPS`, `p95`, `p99`, `error rate`, and JSON outputs in `perf/out`.
