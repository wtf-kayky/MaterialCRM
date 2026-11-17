# MaterialCRM

CRM e sistema de gestão comercial para lojas de materiais de construção, com suporte a múltiplas empresas e filiais.

## Ambientes de produção

O produto terá dois ambientes separados:

- **MaterialCRM Admin:** painel central hospedado para administrar clientes, planos, pagamentos, assinaturas, licenças e instalações.
- **MaterialCRM Local:** aplicação instalada no servidor de cada loja, acessível pela rede LAN e com PostgreSQL e autenticação locais.

O MaterialCRM Local é local-first: vendas, estoque, clientes, financeiro e login não dependem de conexão com a internet depois da instalação. A comunicação com o Admin é assíncrona e usada para sincronização administrativa e de licenças.

## Arquitetura

- **Frontend:** React + TypeScript.
- **Backend:** API REST Spring Boot modular.
- **Banco local:** PostgreSQL em contêiner com volume persistente.
- **Banco central:** separado, pertencente ao MaterialCRM Admin.
- **Implantação Local:** Docker Compose.
- **Acesso:** navegador no próprio servidor por localhost ou nos demais computadores pelo IP LAN do servidor.

## Estado da implementação

- [x] Arquitetura Admin + Local offline-first documentada.
- [x] Serviço PostgreSQL local e volume persistente adicionados ao Compose.
- [x] Autenticação local inicial com BCrypt, JWT HS256 e bootstrap do primeiro administrador.
- [ ] Build Maven e build frontend executados com sucesso.
- [ ] Testes de login, sessão e acesso sem internet.
- [ ] Backup/restauração e acesso LAN validados.
- [ ] Sincronização e licenciamento com MaterialCRM Admin.

**Atenção:** os itens de implementação marcados como concluídos representam código gravado no repositório, não testes de execução. Não utilizar em produção antes de concluir os itens de validação.

## Licenciamento e operação offline

Cada instalação manterá uma licença digital assinada e validável localmente. A chave privada permanece no Admin; a instalação recebe apenas a chave pública. Se a assinatura vencer enquanto a loja estiver offline, as operações locais continuam. A instalação sincroniza e revalida o estado assim que a conexão retornar.

Uma suspensão remota só pode ser aplicada quando a instalação receber a atualização. O Admin deve mostrar a data da última comunicação.

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md)
- [Requisitos](docs/REQUIREMENTS.md)
- [Modelo de dados](docs/DATA-MODEL.md)
- [Segurança](docs/SECURITY-BASELINE.md)
- [Autenticação local](docs/LOCAL-AUTHENTICATION.md)
- [Desenvolvimento local](docs/LOCAL-DEVELOPMENT.md)
- [Etapa 02](docs/STAGE-02-TECHNICAL-FOUNDATION.md)
- [Decisão arquitetural 001 — Supabase (substituída)](docs/decisions/ADR-001-supabase-cloud-auth.md)
- [Decisão arquitetural 002 — Admin e Local offline-first](docs/decisions/ADR-002-admin-and-local-offline-first.md)

O schema Supabase já criado será preservado, mas não é dependência de runtime do MaterialCRM Local.
