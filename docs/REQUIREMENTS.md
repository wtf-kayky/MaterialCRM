# Requisitos do produto

## Ambientes de produção

O produto terá dois ambientes separados:

1. **MaterialCRM Admin:** painel central hospedado, utilizado pelo fornecedor para gerenciar empresas clientes, planos, pagamentos, assinaturas, licenças e instalações.
2. **MaterialCRM Local:** instalação de cada empresa cliente, executada em servidor próprio na rede LAN, com frontend, API, autenticação e PostgreSQL locais.

## Perfis no MaterialCRM Local

| Perfil | Responsabilidade principal |
| --- | --- |
| Administrador da loja | Configuração da empresa, filiais, usuários e permissões |
| Gerente | Acompanhamento da operação e indicadores da filial |
| Vendedor | Atendimento, orçamento e vendas autorizadas |
| Caixa | Recebimentos, abertura e fechamento de caixa |
| Estoquista | Entradas, ajustes, reservas e transferências |

## Requisitos do MaterialCRM Admin

- Cadastrar e administrar empresas clientes e instalações.
- Criar planos, preços, limites e períodos de teste.
- Registrar e acompanhar pagamentos, vencimentos, renovações e cancelamentos.
- Emitir e assinar licenças digitais.
- Acompanhar a última sincronização e a versão instalada por cliente.
- Registrar alterações administrativas em auditoria.
- Permitir que uma instalação desconectada continue operando; o painel deve exibir que o estado remoto pode estar desatualizado.
- Separar o acesso administrativo do acesso dos usuários das lojas.

## Requisitos funcionais prioritários do MaterialCRM Local

### Administração
- Cadastrar empresas e filiais.
- Cadastrar usuários, papéis e permissões por módulo.
- Limitar o acesso do usuário às filiais autorizadas.
- Registrar eventos de auditoria.
- Provisionar funcionários por administrador autorizado.

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

- Login e operações comerciais devem funcionar sem internet após a instalação.
- Frontend, backend e PostgreSQL do Local devem funcionar dentro da LAN.
- O banco local deve manter os dados após reinicialização de contêineres e servidor.
- Backup e restauração devem funcionar sem depender da nuvem.
- Sincronização com Admin é assíncrona e não pode bloquear operações comerciais.
- Falha de internet não pode bloquear vendas, estoque, clientes ou financeiro.
- Expiração de assinatura durante período offline não interrompe a operação local; a licença fica marcada para revalidação quando a conexão retornar.
- Uma suspensão remota só pode ser aplicada localmente depois de recebida pela instalação.
- Operações críticas devem usar transações e validação de concorrência.
- Interface responsiva para computadores de balcão e gestão.
- API documentada por OpenAPI.
- Segredos mantidos somente no servidor e em variáveis de ambiente protegidas.
- Instalações de lojas distintas devem ter isolamento de dados.

## Fora do primeiro escopo de sincronização

A sincronização em tempo real de vendas, estoque e financeiro entre lojas em cidades diferentes não é presumida. Ela exige política explícita de conflitos, consistência e continuidade de operação em cada unidade e será planejada como etapa própria.
