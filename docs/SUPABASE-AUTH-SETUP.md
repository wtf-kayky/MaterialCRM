# Configurar Supabase Auth para o primeiro acesso local

## O que já está implementado no repositório

- Cliente React `@supabase/supabase-js` com sessão persistente e renovação automática.
- Login por e-mail e senha via `signInWithPassword`.
- Logout via `signOut`.
- O access token é enviado no cabeçalho Bearer para a API local.
- Spring Security Resource Server valida emissor, audiência, validade e assinatura via JWKS.
- Endpoint protegido de diagnóstico: `GET /api/v1/me`.
- Cadastro público não é oferecido pela interface.

## 1. Configurar a chave de assinatura JWT

No Dashboard do projeto MaterialCRM:

1. Abra **Authentication → Signing Keys**.
2. Confira a chave ativa e o algoritmo usado para assinar os access tokens.
3. A API está configurada para validar **ES256** ou **RS256** por JWKS.
4. Se o projeto estiver usando somente o JWT secret legado/HS256, siga o procedimento oficial de migração para uma chave assimétrica (ES256 é uma opção adequada).
5. Não revogue a chave anterior durante a migração até confirmar que todos os componentes e sessões relevantes funcionam com a nova chave.

Documentação oficial: https://supabase.com/docs/guides/auth/signing-keys

A publishable API key do projeto é independente da chave de assinatura dos JWTs. Não confunda essas duas chaves.

## 2. Configurar o arquivo .env

Copie `.env.example` para `.env` na raiz. Preencha:

- Dados de conexão PostgreSQL conforme Project Settings → Database.
- `VITE_SUPABASE_PUBLISHABLE_KEY` com a publishable key (prefixo `sb_publishable_`) em Project Settings → API Keys.
- URL, issuer e JWKS já apontam para o projeto MaterialCRM.

A publishable key é pública e pode ser embutida no frontend. Senha do banco, JWT secret, chave privada e secret API key nunca podem ir para o frontend ou GitHub.

## 3. Criar usuário de teste

O cadastro aberto não faz parte do fluxo do CRM. Crie um usuário de teste em **Authentication → Users → Add user** no Dashboard. Use um e-mail e senha de teste controlados por você.

Nesta etapa a conta testa apenas a identidade. Ainda não existe vínculo automático com uma empresa ou filial; esse controle será implementado antes de liberar os módulos comerciais.

## 4. Testar

Com o backend e frontend iniciados:

- Sem token: `GET http://localhost:8081/api/v1/me` deve responder `401 Unauthorized`.
- Com access token válido do Supabase: deve responder `200 OK` com o UUID do usuário no campo `userId`.
- Token expirado, issuer incorreto, audiência incorreta ou assinatura não aceita: deve responder `401 Unauthorized`.
- Login pelo navegador só deve abrir o painel técnico depois que o token for aceito pela API.
- Atualizar a página deve manter a sessão; clicar em Sair deve encerrá-la.

## 5. Limite desta primeira integração

O endpoint confirma a identidade Supabase, mas ainda não consulta `public.app_user`, não verifica vínculo com empresa/filial e não concede permissões comerciais. Essas validações serão implementadas antes de qualquer endpoint operacional.
