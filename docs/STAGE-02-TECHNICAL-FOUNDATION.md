# Etapa 02 — Integração técnica e autenticação

## Entregas em andamento

- API Spring Boot com Java 21 e Actuator.
- PostgreSQL gerenciado no Supabase Cloud.
- Schema inicial de identidade, empresa, filial, papéis, permissões e auditoria aplicado.
- Migrations versionadas pelo Supabase CLI.
- Spring Security Resource Server configurado para validar JWTs Supabase por JWKS.
- Endpoint autenticado `GET /api/v1/me`.
- Frontend React com TypeScript e Vite.
- Docker Compose para iniciar frontend e API localmente.

## O que já foi gravado

- `backend/src/main/java/com/materialcrm/config/SecurityConfig.java`
- `backend/src/main/java/com/materialcrm/api/AuthController.java`
- Dependência `spring-boot-starter-oauth2-resource-server`.
- Validação de issuer, audience, expiração e assinatura assimétrica ES256/RS256.
- Contrato OpenAPI do endpoint de identidade.

## Próximas tarefas desta etapa

1. Configurar/confirmar chave assimétrica de assinatura JWT no Supabase Auth (ES256 ou RS256).
2. Conectar a tela de login React ao Supabase Auth, com dependência versionada e lockfile.
3. Enviar o access token à API local.
4. Relacionar o `sub` do JWT com `public.app_user.auth_user_id`.
5. Implementar autorização por empresa, filial, papel e permissão.
6. Definir papel PostgreSQL de privilégio mínimo para JDBC e validar o isolamento.
7. Executar build Maven, build frontend, testes de autenticação e Docker Compose.

## Critérios de aceite

- [ ] Login real com Supabase Auth.
- [ ] Sem token, API responde 401 nos endpoints protegidos.
- [ ] Token válido responde 200 no endpoint `/api/v1/me`.
- [ ] Token expirado, issuer incorreto e audience incorreta são rejeitados.
- [ ] Usuário sem perfil interno ou sem vínculo ativo não acessa recursos empresariais.
- [ ] Tentativas de cruzar empresa/filial são rejeitadas por testes.
- [ ] API conecta ao Supabase Cloud por TLS usando credencial de banco com privilégio mínimo.
- [ ] Build e execução local validados.

## Limites

O endpoint `/api/v1/me` é apenas a primeira verificação de identidade. Ele ainda não implementa autorização empresarial nem é uma API operacional. Nenhum módulo de vendas ou estoque deve ser iniciado antes dos controles de acesso e testes desta etapa.
