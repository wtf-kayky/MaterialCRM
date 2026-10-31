# Desenvolvimento do MaterialCRM Local

## Topologia atual desta etapa

O Docker Compose já possui PostgreSQL local com volume persistente, API Spring Boot e frontend React:

```text
Computadores da loja (LAN)
          |
          v
Nginx / React :8080
          |
          v
Spring Boot API :8081
          |
          v
PostgreSQL local (rede interna Docker)
          |
          v
Volume persistente materialcrm_database
```

## Estado importante da migração

**O banco operacional já está sendo direcionado para o PostgreSQL local. A autenticação ainda é temporariamente Supabase Auth.** Portanto, esta entrega isolada ainda não permite operar totalmente sem internet: o login será migrado para autenticação local na próxima tarefa. Não trate a instalação como pronta para produção offline.

O banco PostgreSQL não publica a porta 5432 no host por padrão. Somente os serviços Docker acessam o banco pela rede interna.

## Pré-requisitos

- Docker Engine e Docker Compose.
- Para desenvolvimento sem Docker: Node.js 22/npm e JDK 21/Maven 3.9+.
- Na instalação inicial, internet para baixar imagens e dependências. Após completar a migração de autenticação, a operação normal não dependerá da internet.

## Configuração

1. Copie .env.example para .env na raiz do repositório.
2. Defina uma senha forte em POSTGRES_PASSWORD.
3. Durante esta fase de transição, configure também a publishable key Supabase e mantenha os parâmetros JWT de autenticação temporária.
4. Nunca envie .env ao Git ou compartilhe senhas em mensagens.

PowerShell:

```powershell
Copy-Item .env.example .env
```

## Executar os contêineres

Na raiz do repositório:

```bash
docker compose config
docker compose up --build -d
docker compose ps
```

A interface será publicada em http://localhost:8080 no servidor e a API em http://localhost:8081.

Em outro computador da loja, use o IP LAN do servidor, por exemplo http://192.168.0.100:8080. Configure reserva DHCP ou IP estático no servidor e libere a porta 8080 no firewall apenas para a rede local.

## Persistência do PostgreSQL

O serviço database utiliza o volume nomeado materialcrm_database. Reiniciar ou recriar o contêiner não deve remover os dados.

- docker compose down preserva o volume.
- **Não execute docker compose down -v em ambiente com dados que deseja manter:** a opção -v remove volumes.
- Volume não substitui backup. A rotina de backup e restauração será entregue e testada em etapa própria.

## Verificações

```bash
docker compose ps
docker compose logs database
docker compose logs backend
```

O PostgreSQL deve ficar healthy antes da API iniciar. A API possui healthcheck do Actuator; a imagem instala wget para que o healthcheck funcione no contêiner.

## Próximas entregas

1. Remover a dependência de Supabase Auth/JWKS e implementar login local seguro.
2. Criar migrations para o banco local e provisionamento seguro do primeiro administrador.
3. Validar login e operações com a internet desconectada.
4. Implementar backup/restauração e testes de acesso LAN.
5. Construir sincronização e licenciamento com MaterialCRM Admin.
