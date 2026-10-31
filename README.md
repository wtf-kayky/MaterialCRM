# MaterialCRM

CRM e sistema de gestão comercial para lojas de materiais de construção, com suporte a múltiplas empresas e filiais.

## Arquitetura

- **Frontend:** React + TypeScript, executado localmente.
- **Backend:** API REST Spring Boot, executada localmente.
- **Banco de dados:** PostgreSQL no Supabase Cloud.
- **Autenticação:** Supabase Auth; o frontend envia JWT e o backend valida via JWKS.
- **Migrations:** Supabase CLI em `supabase/migrations`.

A aplicação roda em localhost/rede LAN. Login e dados dependem de internet e do Supabase Cloud; não é um modo offline.

## Primeiro acesso local

1. Copie `.env.example` para `.env` e preencha as credenciais do banco e a publishable key Supabase.
2. Crie um usuário de teste em Authentication → Users no Supabase Dashboard.
3. Confira se o projeto usa uma chave de assinatura JWT assimétrica ES256 ou RS256.
4. Execute `docker compose up --build -d`.
5. Acesse `http://localhost:8080`.

Consulte [Desenvolvimento local](docs/LOCAL-DEVELOPMENT.md) e [Configuração Supabase Auth](docs/SUPABASE-AUTH-SETUP.md).

## Estado do projeto

- [x] Schema inicial de identidade e organização no Supabase Cloud (10 tabelas).
- [x] RLS habilitado nas tabelas iniciais e acesso direto de browser revogado.
- [x] API Spring Security Resource Server e endpoint `GET /api/v1/me`.
- [x] Frontend conectado ao fluxo de login/logout Supabase Auth.
- [x] Encaminhamento de `/api` corrigido para preservar o prefixo.
- [ ] Confirmar chave JWT assimétrica no Supabase Auth.
- [ ] Gerar e validar lockfile npm, executar build frontend e Maven.
- [ ] Testar login completo no ambiente local.
- [ ] Implementar autorização multiempresa/multifilial e vínculo `app_user`.

## Documentação

- [Desenvolvimento local](docs/LOCAL-DEVELOPMENT.md)
- [Arquitetura](docs/ARCHITECTURE.md)
- [Modelo de dados](docs/DATA-MODEL.md)
- [Segurança](docs/SECURITY-BASELINE.md)
- [Configuração Supabase Auth](docs/SUPABASE-AUTH-SETUP.md)
- [Integração GitHub ↔ Supabase](docs/SUPABASE-GITHUB-INTEGRATION.md)
- [Etapa 02](docs/STAGE-02-TECHNICAL-FOUNDATION.md)

## Segurança

Nunca versionar `.env`, senha do banco, JWT secret, chave privada ou secret API key. O frontend recebe somente a URL do projeto e a publishable key. O acesso aos dados de negócio continua passando pelo backend.
