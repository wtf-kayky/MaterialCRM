# Autenticação local

O login do MaterialCRM Local é realizado pela API Spring Boot e pelo PostgreSQL da instalação, sem depender de Supabase Auth ou de um serviço de identidade externo.

## Fluxo

1. O administrador inicial é provisionado pelas variáveis de ambiente no primeiro início.
2. O funcionário envia e-mail e senha para `POST /api/v1/auth/login`.
3. A API consulta o usuário no PostgreSQL e verifica a senha com BCrypt.
4. A API emite um JWT HS256 assinado com segredo exclusivo da instalação.
5. O frontend guarda o token em sessionStorage e envia Bearer Token para `GET /api/v1/me`.
6. O token expira após o tempo configurado.

## Primeiro administrador

Defina no .env:
- `BOOTSTRAP_ADMIN_EMAIL`: e-mail do administrador inicial.
- `BOOTSTRAP_ADMIN_PASSWORD`: senha inicial com pelo menos 12 caracteres.
- `APP_JWT_SECRET_BASE64`: chave aleatória Base64 com pelo menos 32 bytes.

Gere uma chave no PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

O bootstrap cria o usuário somente quando a tabela está vazia. Depois do primeiro início, remova a senha de bootstrap do .env. Nunca versione o .env.

## Rotas

| Método | Rota | Acesso |
| --- | --- | --- |
| POST | /api/v1/auth/login | Público, credenciais obrigatórias |
| GET | /api/v1/me | JWT válido |
| GET | /actuator/health | Público, saúde da API |

Todas as demais rotas permanecem negadas por padrão.

## Controles e limites

- Senhas armazenadas com BCrypt (work factor 12).
- JWT HS256 com segredo próprio por instalação.
- Validade padrão de 480 minutos, configurável por `APP_JWT_TTL_MINUTES`.
- Token mantido em sessionStorage e removido ao encerrar a sessão.
- Sem cadastro público ou recuperação de senha nesta entrega.
- Papéis iniciais são identificadores; autorização detalhada por empresa e filial será implementada em etapa posterior.
- Rate limiting, auditoria de autenticação, revogação de tokens e HTTPS na LAN ainda precisam ser concluídos antes da produção.

O projeto Supabase e suas tabelas existentes são preservados, mas não participam do login local.
