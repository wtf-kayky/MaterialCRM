# Etapa 02 — Integração técnica e autenticação

## Entregas gravadas no repositório

- API Spring Boot com Java 21, JDBC e Actuator.
- PostgreSQL gerenciado no Supabase Cloud.
- Schema inicial de identidade, empresa, filial, papéis, permissões e auditoria aplicado.
- Migrations versionadas pelo Supabase CLI.
- Spring Security Resource Server configurado para validar JWTs Supabase via JWKS (ES256/RS256).
- Endpoint autenticado `GET /api/v1/me`.
- Cliente React Supabase Auth com login por senha, persistência de sessão e logout.
- Token enviado ao endpoint de identidade antes de mostrar o painel técnico.
- Docker Compose com parâmetros públicos do Supabase no build do frontend.
- Proxy Vite/Nginx preservando o prefixo `/api`.

## Próximas tarefas

1. Confirmar/configurar chave assimétrica de assinatura JWT no Supabase Auth.
2. Preencher o arquivo local `.env` com conexão PostgreSQL e publishable key.
3. Criar conta de teste em Supabase Auth (sem cadastro público).
4. Gerar lockfile npm e executar build frontend.
5. Executar build Maven e subir Docker Compose.
6. Testar login, renovação de sessão, logout e rejeição de tokens inválidos.
7. Relacionar o `sub` do JWT com `public.app_user.auth_user_id`.
8. Implementar autorização por empresa, filial, papel e permissão.
9. Definir papel PostgreSQL de privilégio mínimo para JDBC e validar isolamento.

## Critérios de aceite

- [ ] Login real com Supabase Auth.
- [ ] Sem token, API responde 401 nos endpoints protegidos.
- [ ] Token válido responde 200 no endpoint `/api/v1/me`.
- [ ] Token expirado, issuer incorreto e audience incorreta são rejeitados.
- [ ] Sessão persiste após atualizar o navegador e logout encerra a sessão.
- [ ] Usuário sem perfil interno ou sem vínculo ativo não acessa recursos empresariais.
- [ ] Tentativas de cruzar empresa/filial são rejeitadas por testes.
- [ ] API conecta ao Supabase Cloud por TLS usando credencial de banco com privilégio mínimo.
- [ ] Build frontend, build Maven e Docker Compose validados.

## Limites

O endpoint `/api/v1/me` é apenas a primeira verificação de identidade. Ele ainda não implementa autorização empresarial nem é uma API operacional. Nenhum módulo de vendas ou estoque deve ser iniciado antes dos controles de acesso e testes desta etapa.
