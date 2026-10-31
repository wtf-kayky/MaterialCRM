# Etapa 02 — Fundação técnica

## Entrega

Disponibilizar a base executável para os módulos futuros:

- API Spring Boot com Java 21 e Actuator;
- PostgreSQL gerenciado no Supabase Cloud;
- autenticação Supabase Auth com validação de JWT no backend;
- frontend React com TypeScript e Vite;
- Nginx como servidor do frontend e proxy da API;
- Docker Compose para iniciar frontend e API localmente;
- migrations versionadas pelo Supabase CLI;
- contrato OpenAPI versionado.

## Como executar

1. Copie `.env.example` para `.env` e preencha os dados de conexão do Supabase Cloud.
2. Execute `docker compose config` para validar a configuração.
3. Na raiz do projeto, execute `docker compose up --build`.
4. Abra `http://localhost:8080`.
5. Consulte `http://localhost:8081/actuator/health` para validar a API.

## Critérios de validação

- A API conecta ao Supabase PostgreSQL por TLS.
- Supabase CLI aplica as migrations no ambiente de desenvolvimento.
- `/actuator/health` responde com estado `UP`.
- A tela inicial consome a API local.
- JWT Supabase é validado no backend.
- A autorização multiempresa/multifilial é coberta por testes positivos e negativos.

## Limites desta etapa

Ainda não há autenticação funcional, telas operacionais, entidades comerciais ou regras de venda completas. Esses itens serão implementados em sequência, sem antecipar módulos comerciais.
