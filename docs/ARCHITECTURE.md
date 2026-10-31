# Arquitetura técnica

## Objetivo

Fornecer uma aplicação comercial confiável para lojas de materiais de construção, executada localmente na rede da empresa, com PostgreSQL e autenticação gerenciados pelo Supabase Cloud e suporte a múltiplas empresas e filiais.

## Princípios

1. Frontend e API rodam localmente e são acessíveis pela LAN.
2. Banco de dados e autenticação ficam no Supabase Cloud; portanto, login e dados exigem internet.
3. Os dados são isolados por empresa e controlados por filial.
4. Movimentações de estoque, vendas e eventos financeiros são rastreáveis.
5. Permissões são verificadas no backend; a interface apenas reflete o acesso permitido.
6. O sistema começa como monólito modular. Integrações futuras permanecem separáveis por contratos REST e eventos de domínio.

## Componentes

```text
Navegador na LAN
       |
       v
Frontend React (servidor local)
       |
       v
API Spring Boot (servidor local)
       |
       +------ PostgreSQL / Supabase Cloud
       |
       +------ Supabase Auth (JWT)
```

### Backend

O backend será um monólito modular em Java/Spring Boot. Os módulos iniciais serão: identidade e acesso, empresas e filiais, catálogo, clientes, fornecedores, estoque, vendas, financeiro, CRM, relatórios e auditoria.

Cada módulo expõe casos de uso e mantém suas regras de domínio. Dependências entre módulos passam por interfaces internas explícitas; a persistência não deve ser acessada diretamente por módulos sem relação de propriedade.

### Frontend

O frontend será React com TypeScript, tema corporativo escuro e componentes reutilizáveis. A interface consumirá exclusivamente a API REST para operações de negócio. Supabase Auth será usado no fluxo de autenticação do cliente, e o access token será enviado à API.

### Persistência e migrations

PostgreSQL no Supabase Cloud será a fonte oficial dos dados. Migrations serão versionadas exclusivamente pelo Supabase CLI em `supabase/migrations`; Flyway não será usado em paralelo.

### Implantação

Docker Compose iniciará API e frontend em um servidor local da empresa. Os computadores da operação acessarão o frontend pela LAN. A API conecta ao Supabase Cloud por TLS. Backups e restauração serão definidos e testados antes do lançamento.

## Segurança e isolamento

- Supabase Auth gerencia credenciais e sessões.
- `public.app_user` referencia `auth.users(id)` e guarda apenas dados internos de perfil.
- Autorização por perfil, módulo, empresa e filial é aplicada no backend.
- Toda operação valida o contexto organizacional no servidor.
- Segredos exclusivamente em variáveis de ambiente; nunca no repositório ou frontend.
- RLS habilitado nas tabelas públicas; acesso direto pela Data API não é concedido nesta arquitetura.
- RLS não substitui a autorização do backend em consultas JDBC privilegiadas.

## Decisões registradas

| Decisão | Diretriz |
| --- | --- |
| Execução da aplicação | Local, em rede LAN |
| Banco | PostgreSQL no Supabase Cloud |
| Autenticação | Supabase Auth |
| Backend | Spring Boot modular |
| Frontend | React + TypeScript |
| Contêineres | Docker Compose para frontend e API |
| Migrations | Supabase CLI |
| Multiunidade | Empresa e filial fazem parte do contexto das operações |
| Estoque | Movimentação e reserva rastreáveis |
| Integrações | REST; microsserviço apenas quando o núcleo estiver estável |
