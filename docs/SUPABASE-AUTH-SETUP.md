# Configurar Supabase Auth para validar JWT na API

## O que já está implementado no repositório

- Spring Security configurado como Resource Server.
- Validação de emissor, audiência, validade e assinatura por chaves públicas JWKS.
- Algoritmos assimétricos aceitos pela API: ES256 e RS256.
- Endpoint protegido de diagnóstico: `GET /api/v1/me`.
- Segredos e URL JWKS fornecidos ao backend por variáveis de ambiente.

## Pré-requisito no Supabase Cloud

O Supabase usa HS256 por padrão em projetos que ainda não migraram para o sistema de signing keys. A API foi preparada para validação local por JWKS e, por isso, requer uma chave assimétrica ativa (ES256 ou RS256). Não se deve presumir que o projeto já esteja usando esse modo.

No Dashboard:

1. Abra **Authentication → Signing Keys**.
2. Confira qual chave está ativa e qual algoritmo está em uso.
3. Se ainda estiver usando o JWT secret legado/HS256, siga o procedimento oficial de migração para Signing Keys e prepare uma chave assimétrica ES256.
4. Teste o login e a validação de tokens antes de revogar qualquer chave antiga. Não revogue a chave anterior enquanto existirem tokens válidos ou componentes que ainda dependam dela.

Documentação oficial: https://supabase.com/docs/guides/auth/signing-keys

## Variáveis do backend

No arquivo local `.env` (não versionar):

```dotenv
SUPABASE_JWT_ISSUER=https://hmkkzlstjpnsfrwqcjax.supabase.co/auth/v1
SUPABASE_JWKS_URI=https://hmkkzlstjpnsfrwqcjax.supabase.co/auth/v1/.well-known/jwks.json
SUPABASE_JWT_AUDIENCE=authenticated
```

O endpoint JWKS publica somente chaves públicas. Nunca copie uma chave privada, JWT secret, senha de banco ou secret API key para o frontend ou GitHub.

## Teste da API

Com o backend em execução:

- Sem token: `GET http://localhost:8081/api/v1/me` deve responder `401 Unauthorized`.
- Com access token válido do Supabase: deve responder `200 OK` com o UUID do usuário no campo `userId`.
- Com token expirado, emissor incorreto ou audiência incorreta: deve responder `401 Unauthorized`.

## Limite desta primeira integração

O endpoint confirma a identidade Supabase, mas ainda não consulta `public.app_user`, não verifica vínculo com empresa/filial e não concede permissões comerciais. Essas validações serão implementadas antes de qualquer endpoint operacional.
