# MaterialCRM

CRM e sistema de gestão comercial para lojas de materiais de construção, com suporte a múltiplas empresas e filiais.

## Arquitetura atual

- **Frontend:** React + TypeScript, executado localmente.
- **Backend:** API REST Spring Boot, executada localmente.
- **Banco de dados:** PostgreSQL no Supabase Cloud.
- **Autenticação:** Supabase Auth; o backend valida os JWTs e aplica as permissões de negócio.
- **Migrations:** Supabase CLI em `supabase/migrations`.

A execução local permite que os computadores da loja acessem o sistema pela rede LAN. **Não é um modo offline**: login e dados dependem de internet e do Supabase Cloud.

## Desenvolvimento

Consulte [docs/LOCAL-DEVELOPMENT.md](docs/LOCAL-DEVELOPMENT.md) para configurar o ambiente local, as variáveis do Supabase e o Docker Compose.

As decisões arquiteturais aprovadas estão em [docs/decisions](docs/decisions/).

## Segurança

- Nunca versionar o arquivo `.env` ou credenciais reais.
- Nunca expor senha do banco ou chave `service_role` no frontend.
- O frontend conversa com a API; não acessa diretamente as tabelas operacionais.
- A API deve validar identidade e autorização por empresa e filial no servidor.

## Progresso

- [x] Decisão de arquitetura: aplicação local + Supabase Cloud.
- [x] Migration inicial de identidade e organização criada.
- [x] Configuração inicial de conexão PostgreSQL TLS no backend.
- [ ] Aplicar e validar a migration no projeto Supabase de desenvolvimento.
- [ ] Implementar e testar validação JWT e autorização multiempresa/multifilial.
- [ ] Validar build e execução local com Docker.
