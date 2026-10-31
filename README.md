# MaterialCRM

CRM e sistema de gestão comercial para lojas de materiais de construção, com suporte a múltiplas empresas e filiais.

## Arquitetura

- **Frontend:** React + TypeScript, executado localmente.
- **Backend:** API REST Spring Boot, executada localmente.
- **Banco de dados:** PostgreSQL no Supabase Cloud.
- **Autenticação:** Supabase Auth; o backend valida JWTs via JWKS.
- **Migrations:** Supabase CLI em `supabase/migrations`.

A aplicação roda em localhost/rede LAN. Login e dados dependem de internet e do Supabase Cloud; não é um modo offline.

## Estado do projeto

- [x] Decisão arquitetural e documentação inicial.
- [x] Schema inicial de organização e identidade criado no Supabase Cloud.
- [x] 10 tabelas com RLS habilitado.
- [x] Índices de chaves estrangeiras complementares.
- [x] Migration inicial e migration de índices versionadas no GitHub.
- [x] API protegida com Spring Security Resource Server e endpoint `GET /api/v1/me`.
- [ ] Configurar chave JWT assimétrica no Supabase Auth e testar a validação real.
- [ ] Conectar a interface de login ao Supabase Auth.
- [ ] Implementar autorização multiempresa/multifilial.
- [ ] Validar build, testes e execução local.

## Documentação

- [Desenvolvimento local](docs/LOCAL-DEVELOPMENT.md)
- [Arquitetura](docs/ARCHITECTURE.md)
- [Modelo de dados](docs/DATA-MODEL.md)
- [Segurança](docs/SECURITY-BASELINE.md)
- [Configuração de Supabase Auth](docs/SUPABASE-AUTH-SETUP.md)
- [Integração GitHub ↔ Supabase](docs/SUPABASE-GITHUB-INTEGRATION.md)
- [Etapa 02](docs/STAGE-02-TECHNICAL-FOUNDATION.md)

## Segurança

Nunca versionar `.env`, senha do banco, JWT secret, chave privada ou secret API key. O frontend não acessa diretamente as tabelas operacionais.
