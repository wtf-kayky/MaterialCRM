# ADR 001 — Supabase Cloud, Auth e execução local

- **Status:** Aprovado
- **Etapa:** 01 — Fundação e decisões arquiteturais
- **Data:** 2026-10-31

## Contexto

O MaterialCRM deve ser executado localmente na rede da empresa, mas utilizar o Supabase Cloud como banco PostgreSQL gerenciado e serviço de autenticação. A execução local não significa operação offline: autenticação e dados em nuvem exigem conexão com a internet.

## Decisão

1. O frontend React/TypeScript e a API Spring Boot serão executados localmente.
2. O Supabase Cloud será a fonte oficial do PostgreSQL e do Supabase Auth.
3. O frontend chamará somente a API Spring Boot para operações de negócio. Não haverá acesso direto do navegador às tabelas operacionais.
4. O backend validará os access tokens JWT emitidos pelo Supabase Auth e aplicará autorização por empresa, filial, papel e permissão.
5. A tabela `app_user` armazenará o perfil interno e referenciará `auth.users(id)`. Senhas e hashes de senha não serão armazenados em `app_user`.
6. Convites ou provisionamento por administrador serão o fluxo de entrada de funcionários. Não haverá cadastro público que conceda acesso automaticamente a uma empresa.
7. As migrations SQL do Supabase CLI serão a única fonte de verdade para o schema. Flyway não será usado em paralelo.
8. Segredos e credenciais de conexão ficarão somente em variáveis de ambiente do backend, nunca no bundle do frontend ou no repositório.
9. O backend verificará explicitamente o vínculo do usuário autenticado com a empresa e a filial solicitadas. Não se presumirá que RLS protege automaticamente consultas JDBC feitas com uma credencial privilegiada.

## Modelo de identidade

- `auth.users`: identidade, e-mail, credenciais e ciclo de autenticação gerenciados pelo Supabase Auth.
- `public.app_user`: perfil de negócio, estado ativo e vínculo com a empresa; contém `auth_user_id` como FK única para `auth.users(id)`.
- `public.company` e `public.branch`: isolamento por empresa e filial.
- `public.role`, `public.permission`, `public.user_role`, `public.role_permission` e `public.user_branch_access`: autorização interna do MaterialCRM.

## Consequências e limites operacionais

- A aplicação pode iniciar em localhost sem publicar o frontend na internet.
- Login e leitura/gravação de dados dependem de internet e do Supabase Cloud.
- Uma queda de energia local interrompe os computadores/servidor locais; uma queda de internet impede o acesso ao Supabase Cloud.
- Backup, restauração, monitoramento e estratégia de indisponibilidade serão definidos antes da entrada em produção.
- A chave `service_role` e a senha do banco jamais serão disponibilizadas ao navegador.

## Critérios de aceite desta decisão

- Nenhuma senha ou hash de senha duplicado em `app_user`.
- Nenhum segredo real versionado.
- Uma única estratégia de migrations.
- API local conectando ao Supabase Cloud por TLS.
- JWT validado no backend e autorização multiempresa/multifilial aplicada no servidor.
