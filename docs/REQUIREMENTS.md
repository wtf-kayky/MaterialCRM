# Requisitos do produto

## Perfis de usuário

| Perfil | Responsabilidade principal |
| --- | --- |
| Administrador | Configuração da empresa, filiais, usuários e permissões |
| Gerente | Acompanhamento da operação e indicadores da filial |
| Vendedor | Atendimento, orçamento e vendas autorizadas |
| Caixa | Recebimentos, abertura e fechamento de caixa |
| Estoquista | Entradas, ajustes, reservas e transferências |

## Requisitos funcionais prioritários

### Administração

- Cadastrar empresas e filiais.
- Cadastrar usuários, perfis e permissões por módulo.
- Limitar o acesso do usuário às filiais autorizadas.
- Registrar eventos de auditoria.

### Comercial

- Cadastrar e pesquisar clientes, contatos e endereços.
- Cadastrar produtos com categoria, código de barras, unidade, preço e custo.
- Suportar unidade, metro, metro quadrado, metro cúbico, quilograma, litro e saco.
- Manter histórico de relacionamento, orçamentos e vendas.

### Estoque

- Registrar compras, recebimentos, entradas, saídas e ajustes.
- Consultar disponibilidade por filial.
- Reservar e liberar quantidades.
- Transferir produtos entre filiais com rastreabilidade.
- Sinalizar estoque mínimo.

### Vendas e financeiro

- Executar vendas de balcão com carrinho, descontos e validação de disponibilidade.
- Registrar pagamentos em dinheiro, Pix, cartão, formas mistas e parcelamento.
- Controlar caixa, sangrias, suprimentos e contas a receber.

## Requisitos não funcionais

- Operação local acessível pela LAN.
- Persistência em PostgreSQL local.
- Operações críticas com transações e validação de concorrência.
- Backup e restauração documentados e testáveis.
- Interface responsiva para computadores de balcão e gestão.
- API documentada por OpenAPI.

