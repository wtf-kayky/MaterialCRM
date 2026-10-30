# Modelo de dados inicial

Este é um modelo conceitual inicial. A modelagem física, índices e migrações serão definidos na Etapa 02 antes da implementação dos módulos.

## Entidades principais

| Área | Entidades |
| --- | --- |
| Organização | Empresa, Filial, Usuário, Perfil, Permissão, Auditoria |
| Catálogo | Produto, Categoria, UnidadeDeMedida, TabelaDePreço |
| Clientes | Cliente, Endereço, Contato, Interação |
| Suprimentos | Fornecedor, Compra, Recebimento |
| Estoque | SaldoEstoque, MovimentoEstoque, ReservaEstoque, Transferência |
| Vendas | Venda, ItemVenda, Orçamento, ItemOrçamento, Devolução |
| Financeiro | Caixa, SessãoCaixa, Pagamento, Parcela, ContaReceber, MovimentoCaixa |

## Relações de domínio

```text
Empresa 1 ─── N Filial
Empresa 1 ─── N Usuário
Filial  1 ─── N SaldoEstoque
Produto 1 ─── N SaldoEstoque
Cliente 1 ─── N Venda e Orçamento
Venda   1 ─── N ItemVenda e Pagamento
Produto 1 ─── N MovimentoEstoque
```

## Convenções

- Entidades persistentes terão identificador UUID.
- Datas de evento serão armazenadas com fuso horário.
- Tabelas transacionais mantêm `created_at`, `created_by` e contexto organizacional quando aplicável.
- Exclusão lógica será avaliada por entidade; documentos e transações concluídas devem ser preservados.

