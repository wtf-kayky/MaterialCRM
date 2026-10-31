# Desenvolvimento local com Supabase Cloud

## Topologia

O frontend e a API rodam localmente. O PostgreSQL e a autenticação ficam no projeto Supabase Cloud.

```text
Navegadores na LAN
       |
       v
Frontend React (localhost:8080 no Docker; 5173 no Vite)
       |
       v
API Spring Boot (localhost:8081)
       |
       +------ Supabase PostgreSQL (TLS)
       |
       +------ Supabase Auth (JWT)
```

**Importante:** localhost/LAN descreve onde a aplicação roda, não um modo offline. Login e dados dependem de internet e do Supabase Cloud.

## Pré-requisitos

- Docker Engine e Docker Compose.
- Node.js 22 e npm 10 para execução de desenvolvimento sem Docker.
- JDK 21 e Maven 3.9+ para executar a API fora do Docker.
- Acesso ao projeto Supabase Cloud do MaterialCRM.
- Credenciais do banco disponíveis em Project Settings → Database.

## Configurar variáveis

1. Copie `.env.example` para `.env` na raiz do repositório (PowerShell: `Copy-Item .env.example .env`).
2. Preencha host, porta, database, usuário e senha apresentados pelo Supabase.
3. Copie a **publishable key** em Project Settings → API Keys para `VITE_SUPABASE_PUBLISHABLE_KEY`. A URL já aponta para o projeto MaterialCRM.
4. Confira as configurações de assinatura JWT em Authentication → Signing Keys; a API valida chaves assimétricas ES256 ou RS256 publicadas no JWKS.
5. Para um backend Spring Boot de longa duração em redes IPv4, prefira o Supavisor **Session mode**. A conexão direta pode exigir IPv6; não use Transaction mode com prepared statements JDBC sem configurar e validar essa compatibilidade.
6. Nunca coloque senha do banco, JWT secret, chave privada, `service_role` ou secret API key em variáveis `VITE_*`, no frontend ou no Git. A publishable key é a única chave Supabase que deve chegar ao navegador.

## Criar uma conta de funcionário para o primeiro acesso

O cadastro público não está habilitado. Para criar a conta de teste:

1. Abra Authentication → Users no Dashboard do Supabase.
2. Use **Add user** para criar um usuário com e-mail e senha de teste.
3. Confirme o usuário, se o painel solicitar.
4. Essa conta permite testar a identidade Supabase. O vínculo com empresa/filial em `public.app_user` será criado e validado na próxima tarefa de autorização empresarial; até lá, não há acesso às áreas comerciais.

## Validar migrations

O schema é versionado exclusivamente em `supabase/migrations`. Flyway não é utilizado.

Depois de instalar o Supabase CLI, valide a migration em um Supabase local (Docker):

```bash
supabase --version
supabase start
supabase db reset
```

O projeto Cloud já recebeu as migrations iniciais. Para mudanças futuras, use `supabase migration new nome_descritivo`, revise o SQL localmente e só aplique no Cloud depois da revisão.

## Executar com Docker

Com o arquivo `.env` preenchido:

```bash
docker compose config
docker compose up --build -d
docker compose ps
```

A interface fica disponível em `http://localhost:8080` por padrão e a API em `http://localhost:8081`. Para outros computadores da rede, use o endereço LAN do servidor e libere somente as portas necessárias no firewall.

## Executar sem Docker (desenvolvimento)

Para desenvolvimento sem Docker, configure no ambiente de execução da API as mesmas variáveis `SPRING_DATASOURCE_*`, `SUPABASE_JWT_ISSUER` e `SUPABASE_JWKS_URI` que o Compose recebe do `.env`. No IntelliJ, configure-as em **Run → Edit Configurations → Environment variables**. Depois execute `mvn spring-boot:run` na pasta `backend`.

Na pasta `frontend`, execute:

```bash
npm install
npm run dev
```

O primeiro `npm install` gera `package-lock.json`; mantenha esse arquivo versionado para instalações reproduzíveis. O Vite lê as variáveis da raiz do repositório e encaminha `/api` para `http://localhost:8081`. Acesse `http://localhost:5173`.

## Identidade e autorização

- Supabase Auth mantém credenciais e emite access tokens JWT.
- `public.app_user` guarda o perfil do funcionário e o vínculo com a empresa; não guarda senha nem hash de senha.
- A API valida assinatura, emissor, audiência e expiração do JWT antes de aceitar a identidade.
- `GET /api/v1/me` é o teste inicial de identidade e exige token válido.
- A API deverá verificar no servidor o vínculo do usuário com a empresa e a filial em cada operação.
- O frontend não acessa diretamente tabelas operacionais.
- Não presuma que RLS protege automaticamente consultas JDBC executadas com uma credencial privilegiada. A estratégia de papel de banco de menor privilégio e/ou contexto de tenant será validada antes de expor operações de negócio.

## Critérios de aceite — primeiro acesso

- [ ] Configurar variáveis locais e conectar o backend ao PostgreSQL Cloud por TLS.
- [ ] Criar conta de teste em Supabase Auth.
- [ ] Login real pelo frontend.
- [ ] API validar o JWT e responder 200 em `/api/v1/me`.
- [ ] Sessão persistir após recarregar a página e ser encerrada pelo botão Sair.
- [ ] Build frontend, build Maven e Docker Compose validados.

## Limites operacionais

Uma queda de internet interrompe acesso ao banco e autenticação em nuvem. Uma queda de energia interrompe a máquina local; um nobreak ajuda apenas com energia, não substitui conexão de internet. Backup e restauração precisam ser testados separadamente.
