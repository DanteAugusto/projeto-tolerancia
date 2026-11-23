# IMDTravel — Tolerância a Falhas (Parte 1 · BASELINE)

Implementação do serviço orquestrador **IMDTravel** para a Parte 1 do projeto da disciplina de
Tolerância a Falhas (DIMAp/UFRN). Conforme exigido na especificação, esta versão (`BASELINE`)
**não possui mecanismos de tolerância a falhas** (sem timeouts configurados, retries, fallbacks,
cache ou filas), servindo como linha de base instrumentada para observação e comparação na Parte 2.

## Equipe

- **Paulo Vitor Lima Borge** — Setup inicial, observabilidade (logs JSON/`X-Correlation-Id`), persistência local e `/health`
- **Jamilly Emilly da Silva Campelo** — Clientes HTTP (`RestClient`), orquestração de compra (`IMDTravelService`) e API REST (`IMDTravelController`)
- **Dante Augusto Bezerra Pinto** — Infraestrutura Docker/Compose, testes unitários, evidências (`tests/`) e documentação

## Linguagem e Framework

- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.1.2 (`spring-boot-starter-webmvc` e `spring-boot-starter-restclient`)
- **Logs estruturados:** Logback (`logback-spring.xml`) emitindo 1 objeto JSON por linha no `stdout` (`ts`, `svc`, `cid`, `event`, `ms`)
- **Persistência local:** arquivo JSON Lines (`/data/sales.jsonl`) em volume nomeado (`sales-data`) com controle de concorrência via `ReentrantReadWriteLock`
- **Infraestrutura:** Docker (build multi-stage com Maven + Eclipse Temurin 21 JRE Alpine) e Docker Compose, integrando com as imagens oficiais `baseline` de **AirlinesHub**, **Exchange** e **Fidelity**

## Arquitetura e fluxo de compra

```
cliente ──POST /buyTicket──▶ IMDTravel ──1. GET  /flight ──▶ AirlinesHub
                                │      ──2. GET  /convert──▶ Exchange
                                │      ──3. POST /sell   ──▶ AirlinesHub
                                │      ──4. POST /bonus  ──▶ Fidelity
                                ▼
                     /data/sales.jsonl (GET /sales)
```

- Apenas a porta do `IMDTravel` (`8080`, configurável por `IMDTRAVEL_HOST_PORT`) é exposta ao host.
- O cabeçalho `X-Correlation-Id` é aproveitado da requisição ou gerado via UUID, retornado na resposta e propagado nas quatro chamadas de saída.
- Todos os serviços expõem `GET /health` usado nos `healthcheck` do `docker-compose.yml` com `depends_on: condition: service_healthy`. No `IMDTravel`, `/health` verifica permissão de escrita no arquivo de vendas.

## Catálogo de voos e configuração

Configurado diretamente via variáveis de ambiente no [`docker-compose.yml`](docker-compose.yml):

| Voo | Preço em `2026-12-01` | Acréscimo por dia | Preço em `2026-12-07` | Assentos/dia |
|---|---|---|---|---|
| `IMD100` | US$ 350,50 | US$ 12,25 | US$ 424,00 | 20 |
| `IMD200` | US$ 420,50 | US$ 12,25 | US$ 494,00 | 20 |
| `IMD300` | US$ 510,50 | US$ 12,25 | US$ 584,00 | 20 |

- **Período:** 7 dias consecutivos (`2026-12-01` a `2026-12-07`), totalizando 21 pares `(voo, dia)` × 20 assentos.
- **AirlinesHub:** latência de `POST /sell` entre 300 ms e 900 ms.
- **Exchange:** taxa inicial `5.50`, variando até ±0,3% a cada 10 s dentro da faixa `[5.00, 6.00]`.

## Como executar

Pré-requisitos: Docker com Compose v2, `bash`, `curl` e `jq`.

```bash
# Subir todos os serviços (compila o IMDTravel e aguarda os 4 ficarem healthy)
docker compose up -d --build --wait

# Conferir saúde
curl -s http://localhost:8080/health

# Realizar uma compra
curl -i -H 'Content-Type: application/json' \
     -H 'X-Correlation-Id: teste-manual-1' \
     -d '{"flight":"IMD100","day":"2026-12-01","user":"ana"}' \
     http://localhost:8080/buyTicket

# Consultar vendas registradas
curl -s http://localhost:8080/sales | jq '.'
```

## Testes realizados

As evidências completas (saídas de terminal, logs JSON correlacionados e snapshot de vendas) estão na pasta [`tests/`](tests/):

1. **Testes unitários (`./mvnw test`):** validação do carregamento do contexto Spring, filtro de correlação (`CorrelationAndLoggingFilterTest`), persistência/leitura no `SalesFileRepositoryTest` e validações do `IMDTravelControllerTest`.
2. **Testes funcionais (`./testes.sh --reiniciar`):**
   - `GET /health` nos quatro serviços;
   - Compra completa (`201`), verificação dos 9 campos, cálculo `valueBRL = valueUSD × rate`, bônus inteiro, decremento de estoque, registro em `/sales` e crédito no `Fidelity` com o mesmo `X-Correlation-Id`;
   - Geração automática de `X-Correlation-Id` quando ausente;
   - Determinismo de preço e estabilidade de taxa entre ticks;
   - Validação de erros (`404` voo/dia inexistente, `400` campos ausentes/vazios ou JSON malformado) sem alterar estoque ou `/sales`;
   - Esgotamento sob concorrência (25 compras simultâneas para 20 assentos → `20 × 201` e `5 × 409`, estoque final `0`).
3. **Teste de falha sem tolerância (`./testes.sh --com-falhas`):**
   - Com o container `fidelity` parado, a compra falha após o passo irreversível (`POST /sell`), retornando HTTP `500`, consumindo o assento no `AirlinesHub` e não registrando a venda em `/sales`, evidenciando a janela de inconsistência e a ausência de timeout na Parte 1.
