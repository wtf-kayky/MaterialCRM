# Desenvolvimento do MaterialCRM Local

## Configuração

1. Copie `.env.example` para `.env`.
2. Defina uma senha forte para PostgreSQL.
3. Gere `APP_JWT_SECRET_BASE64` com 32 bytes aleatórios em Base64. No PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

4. Antes do primeiro início, informe `BOOTSTRAP_ADMIN_EMAIL` e uma `BOOTSTRAP_ADMIN_PASSWORD` com pelo menos 12 caracteres.
5. Nunca versione ou compartilhe o arquivo .env.

## Inicialização

Na raiz do projeto:

```powershell
docker compose config
docker compose up --build -d
docker compose ps
```

O PostgreSQL é privado na rede Docker e usa o volume `materialcrm_database`. A interface fica em `http://localhost:8080` e a API em `http://localhost:8081`. Em outros computadores da loja, use `http://IP-DO-SERVIDOR:8080`.

## Primeiro administrador

No primeiro início, a API cria o administrador informado no .env somente se a tabela de usuários estiver vazia. A senha é armazenada com BCrypt. Depois de confirmar a criação, remova `BOOTSTRAP_ADMIN_PASSWORD` do .env e reinicie o backend. Não existe cadastro público.

## Autenticação

O login é feito localmente por `POST /api/v1/auth/login`. A API emite JWT HS256 assinado por uma chave exclusiva da instalação. O frontend mantém o token em sessionStorage e usa-o para chamar `GET /api/v1/me`. O banco aplica migrations automaticamente com Flyway.

## Estado de validação

O código foi gravado no GitHub, mas ainda precisa de build Maven, build frontend e execução real via Docker Compose. Teste também o login com a internet desligada antes de considerar a instalação pronta para produção.
