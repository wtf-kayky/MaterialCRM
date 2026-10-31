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

## Licenciamento e operação offline

Cada instalação mantém uma licença digital assinada e validável localmente. A chave privada permanece no Admin; a instalação recebe apenas a chave pública. Se a assinatura vencer enquanto a loja estiver offline, as operações locais continuam. A instalação sincroniza e revalida o estado assim que a conexão retornar.

Uma suspensão remota só pode ser aplicada quando a instalação receber a atualização. O Admin deve mostrar a data da última comunicação.

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md)
- [Requisitos](docs/REQUIREMENTS.md)
- [Modelo de dados](docs/DATA-MODEL.md)
- [Segurança](docs/SECURITY-BASELINE.md)
- [Etapa 02](docs/STAGE-02-TECHNICAL-FOUNDATION.md)
- [Decisão arquitetural 001 — Supabase](docs/decisions/ADR-001-supabase-cloud-auth.md)
- [Decisão arquitetural 002 — Admin e Local offline-first](docs/decisions/ADR-002-admin-and-local-offline-first.md)

## Estado do projeto

A arquitetura anterior baseada em Supabase Cloud como banco operacional e autenticação obrigatória foi substituída nesta linha de desenvolvimento pela arquitetura local-first. O schema Supabase já criado será preservado; não é dependência de runtime do MaterialCRM Local.

A fundação ainda precisa ser implementada e validada. Não considerar o ambiente local pronto para produção até concluir builds, testes de autenticação, persistência, backup/restauração e acesso por LAN.
