# Evidências dos Testes — Parte 1 (BASELINE)

Esta pasta concentra as evidências de execução dos testes funcionais, de concorrência,
de injeção de falha (Fidelity parado) e dos logs estruturados com `X-Correlation-Id`.

## Arquivos de evidência

- `testes-normal.txt` — saída completa de `./testes.sh --reiniciar`
- `testes-com-falhas.txt` — saída completa de `./testes.sh --com-falhas` (Fidelity fora do ar → HTTP 500, assento consumido, sem registro em `/sales`)
- `compra-exemplo-http.txt` — requisição/resposta HTTP de uma compra com `X-Correlation-Id` explícito
- `logs-correlacao.jsonl` — linhas de log JSON dos 4 containers filtradas pelo mesmo `cid`
- `sales-evidencia.json` — registro local de vendas retornado por `GET /sales`
