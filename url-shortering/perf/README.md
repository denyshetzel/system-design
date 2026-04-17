# Performance Tests (k6)

Este diretório define uma base profissional e reutilizável de teste de carga para requisitos não funcionais (NFR), com foco em latência, throughput e taxa de erro.

## Cenários disponíveis

- `k6/smoke-api.js`: validação rápida (sanity) para confirmar saúde da API.
- `k6/load-api.js`: carga contínua com `constant-arrival-rate` e thresholds de SLA.

## Pré-requisitos

- Docker em execução.
- API disponível em `http://localhost:8081` (ou definir `BASE_URL`).

## Execução local com Docker

### 1) Smoke test

```powershell
docker run --rm -i `
  -e BASE_URL=http://host.docker.internal:8081 `
  -v ${PWD}/perf/k6:/scripts `
  grafana/k6 run /scripts/smoke-api.js
```

### 2) Load test com SLA

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

## Interpretação para NFR

- `http_req_duration p(95)` menor que `P95_MS`.
- `http_req_duration p(99)` menor que `P99_MS`.
- `http_req_failed` menor que `ERROR_RATE`.
- `checks` maior que `99%`.

Se qualquer threshold falhar, o teste deve ser considerado reprovado para o SLA definido.

## Uso contínuo (recomendado)

- Rodar `smoke-api.js` em PR.
- Rodar `load-api.js` em nightly/release.
- Versionar os thresholds de SLA junto com o código para rastreabilidade.

## Front-end

Para web front-end, use em conjunto:

- `k6` para tráfego HTTP/API em escala.
- `Lighthouse CI` para Web Vitals (LCP, CLS, INP), tempo de render e orçamento de performance.

Esse combo cobre backend e experiência de usuário final com ferramentas padrão de mercado.
