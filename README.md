# Tesoura API

Backend do microSaaS de agendamento para cabeleireiros e salões pequenos.

## Stack

- Java 21 + Spring Boot 3.3
- PostgreSQL + Flyway (migrations)
- Spring Security + JWT
- Quartz (jobs de notificação agendados por evento)
- Testcontainers (testes de integração)

## Rodando localmente

```bash
# Sobe um Postgres local via Docker
docker run --name tesoura-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=tesoura -p 5432:5432 -d postgres:16

# Roda a aplicação com o profile de dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A API sobe em `http://localhost:8080`. Migrations do Flyway rodam automaticamente no boot.

## Estrutura

Organização por feature (vertical slice) em vez de por camada técnica — cada pasta em
`src/main/java/com/tesoura/api/` contém entidade, repository, service e controller da
sua própria área (`appointment/`, `client/`, `professional/`, etc.), além de `config/`
e `shared/` para código transversal (contexto de tenant, tratamento de erros).

Ver `docs/estrutura_backend_springboot.md` para a explicação completa das decisões de
arquitetura, incluindo a estratégia de multi-tenancy por `salon_id`.

## Multi-tenancy

Cada salão é isolado por `salon_id`. O `TenantContextFilter` extrai o tenant do JWT e
popula um `ThreadLocal` (`TenantContext`) consumido pelos services; um filtro do
Hibernate reforça esse isolamento também no nível de query.
