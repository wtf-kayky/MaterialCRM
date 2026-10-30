# Arquitetura técnica

## Objetivo

Fornecer uma aplicação comercial confiável para lojas de materiais de construção, operando prioritariamente na rede local e atendendo múltiplas empresas e filiais.

## Princípios

1. A operação da loja deve continuar disponível sem internet.
2. Os dados devem ser isolados por empresa e controlados por filial.
3. Movimentações de estoque, vendas e eventos financeiros devem ser rastreáveis.
4. Permissões são verificadas no backend; a interface apenas reflete o acesso permitido.
5. O sistema começa como monólito modular. Integrações futuras permanecem separáveis por contratos REST e eventos de domínio.

## Componentes

```text
Navegador na LAN
        │
        ▼
Frontend React ───► API Spring Boot ───► PostgreSQL local
                              │
                              └────────► serviços externos (futuro)
```

### Backend

O backend será um monólito modular em Java/Spring Boot. Os módulos iniciais serão: identidade e acesso, empresas e filiais, catálogo, clientes, fornecedores, estoque, vendas, financeiro, CRM, relatórios e auditoria.

Cada módulo expõe casos de uso e mantém suas regras de domínio. Dependências entre módulos passam por interfaces internas explícitas; a persistência não deve ser acessada diretamente por módulos sem relação de propriedade.

### Frontend

O frontend será React com TypeScript, tema corporativo escuro e componentes reutilizáveis. A interface consumirá exclusivamente a API REST, preservando o backend como fonte de autorização e regras de negócio.

### Persistência

PostgreSQL será a fonte de dados operacional. Registros transacionais importantes usarão identificadores imutáveis, datas com fuso explícito e trilha de auditoria. Alterações de estoque serão registradas como movimentos, sem sobrescrever o histórico.

### Implantação

Docker Compose iniciará API, frontend e PostgreSQL em um servidor local da empresa. Os computadores da operação acessarão o frontend pela LAN. Backups serão gerados e testados antes do lançamento.

## Segurança e isolamento

- Autenticação com credenciais protegidas por hash forte.
- Autorização por perfil, módulo, empresa e filial.
- Auditoria para eventos administrativos e operacionais relevantes.
- Validação de empresa e filial no contexto de cada operação.
- Segredos exclusivamente em variáveis de ambiente; nunca no repositório.

## Decisões registradas

| Decisão | Diretriz |
| --- | --- |
| Execução | Local, em rede LAN |
| Banco | PostgreSQL local |
| Backend | Spring Boot modular |
| Frontend | React + TypeScript |
| Contêineres | Docker Compose |
| Multiunidade | Empresa e filial fazem parte do contexto das operações |
| Estoque | Movimentação e reserva rastreáveis |
| Integrações | REST; microsserviço apenas quando o núcleo estiver estável |

