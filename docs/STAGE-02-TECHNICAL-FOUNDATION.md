# Etapa 02 — Fundação técnica

## Entrega

Esta entrega disponibiliza a base executável para os módulos futuros:

- API Spring Boot com Java 21, Actuator e Flyway;
- PostgreSQL 16 com volume persistente;
- frontend React com TypeScript e Vite;
- Nginx como servidor do frontend e proxy da API;
- Docker Compose para inicializar os três serviços;
- primeira migração do banco e contrato OpenAPI versionado.

## Como executar

1. Copie `.env.example` para `.env` e escolha uma senha segura em `POSTGRES_PASSWORD`.
2. Na raiz do projeto, execute `docker compose up --build`.
3. Abra `http://localhost:8080`.
4. Consulte `http://localhost:8081/actuator/health` para validar a API.

## Critérios de validação

- PostgreSQL deve ficar saudável antes de a API iniciar.
- Flyway deve aplicar `V1__create_application_metadata.sql`.
- `/actuator/health` deve responder com estado `UP`.
- A tela inicial deve indicar que a API local está disponível.

## Limites desta etapa

Ainda não há autenticação, telas operacionais, entidades comerciais ou regras de venda. Esses itens começam nas etapas posteriores, a partir da base criada aqui.

