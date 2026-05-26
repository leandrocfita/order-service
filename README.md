# order-service — Processamento de Pedidos e Pagamentos

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-latest-47A248.svg)](https://www.mongodb.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-black.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)

Microsserviço responsável pelo processamento assíncrono de pedidos e integração com o gateway de pagamentos no ecossistema Cheffy, desenvolvido como Tech Challenge da Pós-Graduação em Arquitetura e Desenvolvimento Java da FIAP.

---

## O que é o order-service?

O `order-service` é o microsserviço responsável por receber pedidos criados pelo `cheffy-api`, processá-los junto ao gateway de pagamentos (`procpag`) e devolver o resultado do pagamento de forma assíncrona via Apache Kafka. Todo o ciclo de vida do pedido — desde a criação até o pagamento ou cancelamento — é gerenciado por este serviço com suporte a retentativas automáticas e circuit breaker.

---

## Posição na Arquitetura de Microsserviços

```
┌─────────────┐     JWT      ┌──────────────────┐
│   Cliente   │─────────────▶│   auth-service   │  :8085
│  (HTTP/REST)│              │  OAuth2 + RS256   │──── PostgreSQL (auth_service)
└──────┬──────┘              └──────────────────┘
       │ Bearer Token
       ▼
┌──────────────────┐  Feign  ┌──────────────────┐
│   cheffy-api     │────────▶│   auth-service   │
│  (API principal) │  :8080  │  (registro user) │
└──────┬───────────┘         └──────────────────┘
       │ PostgreSQL (cheffy)
       │
       │ Kafka: order.created
       ▼
┌──────────────────┐  Feign  ┌──────────────────┐
│  order-service   │────────▶│    procpag       │  :8089
│  Pedidos + Pag.  │  :8083  │  Gateway Pagto.  │
└──────┬───────────┘         └──────────────────┘
       │ MongoDB (cheffy-order-service)
       │
       │ Kafka: order.status-change
       └──────────────────▶ cheffy-api (atualiza status)
```

---

## Stack Técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.0 |
| Persistência | Spring Data MongoDB |
| Banco de dados | MongoDB (`cheffy-order-service`) |
| Mensageria | Apache Kafka (KRaft) |
| Integração externa | OpenFeign + Resilience4j (Circuit Breaker + Retry) |
| Autenticação | Spring Security + OAuth2 Resource Server (JWT RS256) |
| Mapeamento | MapStruct 1.6.3 |
| Build | Maven |
| Containerização | Docker + Docker Compose |

---

## Arquitetura Interna (Hexagonal / Ports & Adapters)

O `order-service` segue a Arquitetura Hexagonal (Ports & Adapters), organizando o código em camadas bem definidas:

```
Infrastructure (in)  →  Kafka Consumers · REST Controllers · Security
Application          →  Use Cases (Services) · Input/Output Ports · Mappers
Domain               →  Order · PaymentStatus · Exceptions
Infrastructure (out) →  MongoDB Adapter · Payment Feign Client · Kafka Producers
```

### Ports de Entrada (Input Ports)

| Interface | Método | Responsabilidade |
|---|---|---|
| `PlaceOrderInputPort` | `execute(PlaceOrderCommandRecord)` | Processa um pedido e solicita o pagamento |
| `CancelOrderInputPort` | `cancelOrder(String orderId)` | Cancela um pedido existente |
| `OrderStatusInputPort` | `checkOrderStatus(UUID orderId)` | Consulta o status atual de um pedido |

### Ports de Saída (Output Ports)

| Interface | Responsabilidade |
|---|---|
| `OrderRepositoryOutputPort` | Persistência no MongoDB |
| `PaymentOutputPort` | Chamada ao gateway de pagamentos via Feign |
| `OrderStatusChangeOutputPort` | Publicação de eventos no tópico `order.status-change` |
| `ReprocessOrderOutputPort` | Publicação de eventos no tópico `order.pending-payment` |

---

## Fluxo de Processamento de Pedido

### Caminho feliz (Happy Path)

```
[cheffy-api] ──Kafka: order.created──▶ [PlaceOrderConsumer]
                                              │
                                              ▼
                                    [PlaceOrderService]
                                              │
                              ┌───────────────┼───────────────┐
                              │               │               │
                         Salva no        Chama procpag   Publica evento
                          MongoDB       (requestPayment)  order.status-change
                              │               │               │
                              └───────────────┼───────────────┘
                                              │
                                    Consulta status do pagamento
                                    (getPaymentStatus)
                                              │
                              ┌───────────────┼───────────────┐
                              │               │               │
                         Atualiza        Atualiza       Publica evento
                          MongoDB         status        order.status-change
                                              │
                                     [cheffy-api recebe
                                      o status final]
```

### Fluxo de Reprocessamento (Falha no Pagamento)

Quando o `procpag` está indisponível ou retorna erro, o serviço executa o seguinte fluxo:

1. O pedido tem seu status atualizado para `PENDING` no MongoDB
2. Um evento é publicado no tópico `order.pending-payment`
3. O `ReprocessOrderConsumer` consome o evento
4. Se `attempt < 2`: o pedido é reprocessado via `PlaceOrderService`
5. Se `attempt == 2` (máximo atingido): o pedido é cancelado via `CancelOrderService`

```
[PaymentAdapter] ─── exception ──▶ [PlaceOrderService]
                                          │
                             Status → PENDING + salva MongoDB
                                          │
                        Publica em order.pending-payment
                                          │
                                  [ReprocessOrderConsumer]
                                          │
                         ┌────────────────┴────────────────┐
                    attempt < 2                       attempt == 2
                         │                                  │
                  Reprocessa pedido                  Cancela pedido
```

---

## Tópicos Kafka

| Tópico | Direção | Grupo de Consumo | Descrição |
|---|---|---|---|
| `order.created` | Consumido | `order.created-group` | Recebe novos pedidos do `cheffy-api` |
| `order.pending-payment` | Consumido / Produzido | `order.pending-payment.group.` | Fila de reprocessamento de pedidos com falha |
| `order.status-change` | Produzido | — | Notifica o `cheffy-api` sobre mudanças de status |

---

## Resiliência

O serviço utiliza Resilience4j para proteger a integração com o gateway de pagamentos.

### Retry

| Parâmetro | Valor |
|---|---|
| Máximo de tentativas | 6 |
| Tempo de espera entre tentativas | 3 segundos |
| Tipo de backoff | Exponential |

### Circuit Breaker

| Parâmetro | Valor |
|---|---|
| Tamanho da janela deslizante | 10 chamadas |
| Mínimo de chamadas para avaliação | 10 |
| Taxa de falha para abrir o circuito | 50% |
| Tempo em estado aberto | 30 segundos |

Quando o circuit breaker está aberto, a chamada ao `procpag` é bloqueada imediatamente e o pedido segue para o fluxo de reprocessamento.

---

## Endpoints REST

> Acesse o Swagger em `http://localhost:8083/swagger-ui.html`

**Base URL:** `http://localhost:8083`

| Recurso | Método | Path | Auth | Descrição |
|---|---|---|---|---|
| Processar pedido | POST | `/v1/order` | Bearer Token | Inicia o processamento de um pedido |
| Status do pedido | GET | `/v1/order?orderId={uuid}` | Bearer Token | Consulta o status atual de um pedido |

**Exemplo de corpo para `POST /v1/order`:**
```json
{
  "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "totalAmount": 150.00
}
```

**Exemplo de resposta:**
```json
{
  "orderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "status": "pago"
}
```

---

## Segurança

O serviço valida tokens JWT emitidos pelo `auth-service` via endpoint JWKS público:

- Tokens assinados com RSA (RS256)
- Chave pública obtida em `http://auth-service:8085/.well-known/jwks.json`

**Endpoints públicos (sem autenticação):**
- `/actuator/**` — Health checks
- `/v3/api-docs/**` — Swagger API docs
- `/swagger-ui/**` e `/swagger-ui.html`

Todos os demais endpoints exigem `Authorization: Bearer <token>`.

---

## Status de Pagamento

| Status | Valor (JSON) | Descrição |
|---|---|---|
| `CREATED` | `"criado"` | Pedido criado, aguardando processamento |
| `SENT_TO_PAYMENT_GATEWAY` | `"enviado"` | Pedido enviado ao gateway de pagamentos |
| `PENDING` | `"pendente"` | Falha temporária — aguardando reprocessamento |
| `PAID` | `"pago"` | Pagamento confirmado com sucesso |
| `CANCELED` | `"cancelado"` | Pedido cancelado após esgotar as tentativas |

---

## ⚠️ Ressalva: Conversão de `BigDecimal` para `Integer` no envio ao `procpag`

Em `PlaceOrderService.java` (linhas 71–75), o valor total do pedido (`totalAmount`) é convertido de `BigDecimal` para `Integer` antes de ser enviado ao gateway de pagamentos:

```java
paymentOutputPort.requestPayment(new PaymentRequestRecord(
    request.totalAmount().intValue(), // conversão BigDecimal → Integer
    request.orderId().toString(),
    request.orderId().toString()
));
```

**Por que isso é feito?**

A API externa `procpag` aceita o campo `valor` **somente como inteiro** (`Integer`). Isso é uma limitação da API do gateway de pagamentos que não está sob nosso controle. Embora a conversão pareça contraintuitiva — já que valores monetários deveriam ser representados com precisão decimal — fomos obrigados a realizá-la para garantir a compatibilidade com a interface do `procpag`.

> **Impacto:** Valores com casas decimais são truncados (não arredondados) durante a conversão. Exemplo: `R$ 99,90` é enviado ao gateway como `99`. Isso deve ser considerado ao definir os valores dos pedidos no `cheffy-api`.

---

## Como Executar

### Via Docker Compose (recomendado — stack completa)

O `order-service` faz parte da stack do `cheffy-api`. Para subir todos os serviços:

```bash
git clone https://github.com/leandrocfita/cheffy-api.git
cd cheffy-api
```

Crie o arquivo `.env` na raiz (veja instruções no README do `cheffy-api`), então:

```bash
docker compose up --build
```

O `order-service` estará disponível em `http://localhost:8083`.

```bash
docker compose down      # mantém os dados
docker compose down -v   # remove volumes (apaga dados)
```

### Localmente (desenvolvimento isolado)

**Pré-requisitos:**
- Java 21
- Maven 3.9+
- MongoDB rodando em `localhost:27017`
- Apache Kafka rodando em `localhost:9092`
- `auth-service` rodando em `localhost:8085`
- `procpag` rodando em `localhost:8089`

```bash
cd order-service
mvn spring-boot:run
```

---

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `SPRING_DATA_MONGODB_URI` | `mongodb://admin:admin@localhost:27017/cheffy-order-service?authSource=admin` | URI de conexão com o MongoDB |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Endereço do broker Kafka |
| `PAYMENT_API_URL` | `http://localhost:8089` | URL do gateway de pagamentos (`procpag`) |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | `http://localhost:8085/.well-known/jwks.json` | URI da chave pública JWT para validação de tokens |

---

## UIs de Administração

| Ferramenta | URL | Credenciais |
|---|---|---|
| Swagger — order-service | `http://localhost:8083/swagger-ui.html` | — |
| Kafka UI | `http://localhost:8090` | — |
| Mongo Express | `http://localhost:8081` | `admin` / `pass` |

---

## 👥 Equipe

- Leandro Fita
- Igor Costa
- Rodrigo Ferreira
- Thiago Soares
- Victor Reis

## 📄 Licença

Este projeto foi desenvolvido como parte do Tech Challenge da FIAP e é disponibilizado para fins educacionais.

## 🤝 Contribuindo

Este é um projeto acadêmico, mas sugestões e feedback são bem-vindos!

## 📞 Contato

Para dúvidas ou sugestões, abra uma issue no repositório.

---

Desenvolvido pela equipe Cheffy - FIAP 2026
