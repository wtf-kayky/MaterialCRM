# Desenvolvimento local com Supabase Cloud

## Topologia

O frontend e a API rodam localmente. O PostgreSQL e a autenticação ficam no projeto Supabase Cloud.

```text
Navegadores na LAN
       |
       v
Frontend React (máquina local)
       |
       v
API Spring Boot (máquina local)
       |
       +------ Supabase PostgreSQL (TLS)
       |
       +------ Supabase Auth (JWT)
```

**Importante:** localhost/LAN descreve onde a aplicação roda, não um modo offline. Login e dados dependem de internet e do Supabase Cloud.

## Pré-requisitos

- Docker Engine e Docker Compose.
- Acesso ao projeto Supabase Cloud do MaterialCRM.
- Supabase CLI instalado.
- Credenciais do banco disponíveis em Project Settings → Database.

## Configurar variáveis

1. Copie `.env.example` para `.env`.
2. Preencha host, porta, database, usuário e senha apresentados pelo Supabase.
3. Para um backend Spring Boot de longa duração em redes IPv4, prefira o Supavisor **Session mode**. A conexão direta pode exigir IPv6; não use Transaction mode com prepared statements JDBC sem configurar e validar essa compatibilidade.
4. Nunca coloque senha do banco, `service_role` ou outro segredo em variáveis `VITE_*`, no frontend ou no Git.

## Validar migrations

O schema é versionado exclusivamente em `supabase/migrations`. Flyway não é utilizado.

Depois de instalar o Supabase CLI, valide a migration em um Supabase local (Docker):

```bash
supabase --version
supabase start
supabase db reset
```

Se o reset local concluir sem erros, autentique e vincule o repositório ao projeto Cloud. Revise a migration e o destino antes de enviar qualquer alteração ao banco remoto:

```bash
supabase login
supabase link --project-ref hmkkzlstjpnsfrwqcjax
supabase migration list
supabase db push
```

Para criar migrations futuras, use `supabase migration new nome_descritivo` e escreva o SQL no arquivo gerado. Não crie uma segunda trilha Flyway.

A migration inicial cria o perfil `public.app_user` ligado por FK a `auth.users(id)`; ela não cria contas de funcionários automaticamente. As tabelas públicas começam com RLS habilitado e sem acesso direto concedido a `anon` ou `authenticated`.

## Executar a aplicação

Com o arquivo `.env` preenchido:

```bash
docker compose config
docker compose up --build -d
docker compose ps
```

A interface fica disponível em `http://localhost:8080` por padrão e a API em `http://localhost:8081`. Para outros computadores da rede, use o endereço LAN do servidor e libere somente as portas necessárias no firewall.

## Identidade e autorização

- Supabase Auth mantém credenciais e emite access tokens JWT.
- `public.app_user` guarda o perfil do funcionário e o vínculo com a empresa; não guarda senha nem hash de senha.
- A API deverá validar assinatura, emissor, audiência e expiração do JWT antes de aceitar a identidade.
- A API deverá verificar no servidor o vínculo do usuário com a empresa e a filial em cada operação.
- O frontend não acessa diretamente tabelas operacionais.
- Não presuma que RLS protege automaticamente consultas JDBC executadas com uma credencial privilegiada. A estratégia de papel de banco de menor privilégio e/ou contexto de tenant será validada antes de expor operações de negócio.

## Critérios de aceite da fundação

- [ ] Migration inicial aplicada e validada em Supabase local.
- [ ] Migration aplicada em ambiente Supabase Cloud de desenvolvimento após revisão.
- [ ] API inicia localmente e conecta ao banco usando TLS.
- [ ] Frontend e API continuam executando localmente.
- [ ] JWT Supabase validado pela API.
- [ ] Autorização multiempresa/multifilial validada por testes negativos.
- [ ] Estratégia de backup, restauração e indisponibilidade documentada antes da produção.

## Limites operacionais

Uma queda de internet interrompe acesso ao banco e autenticação em nuvem. Uma queda de energia interrompe a máquina local; um nobreak ajuda apenas com energia, não substitui conexão de internet. Backup e restauração precisam ser testados separadamente.
