# Modelo de dados inicial

Este modelo conceitual orienta a evolução do schema. A primeira migration física está em `supabase/migrations`; as demais áreas serão modeladas nas etapas correspondentes.

## Entidades principais

| Área | Entidades |
| --- | --- |
| Organização | Empresa, Filial, Perfil interno de usuário, Papel, Permissão, Auditoria |
| Identidade | Identidade e credenciais em `auth.users` (Supabase Auth) |
| Catálogo | Produto, Categoria, UnidadeDeMedida, TabelaDePreço |
| Clientes | Cliente, Endereço, Contato, Interação |
| Suprimentos | Fornecedor, Compra, Recebimento |
| Estoque | SaldoEstoque, MovimentoEstoque, ReservaEstoque, Transferência |
| Vendas | Venda, ItemVenda, Orçamento, ItemOrçamento, Devolução |
| Financeiro | Caixa, SessãoCaixa, Pagamento, Parcela, ContaReceber, MovimentoCaixa |

## Relações de domínio

```text
auth.users 1 ─── 0..1 app_user
Empresa    1 ─── N Filial
Empresa    1 ─── N app_user
Empresa    1 ─── N Papel
app_user   N ─── N Papel
Papel      N ─── N Permissão
app_user   N ─── N Filial autorizada
Filial     1 ─── N SaldoEstoque
Produto    1 ─── N SaldoEstoque
Cliente    1 ─── N Venda e Orçamento
Venda      1 ─── N ItemVenda e Pagamento
Produto    1 ─── N MovimentoEstoque
```

## Identidade e autorização

- `auth.users` é administrada pelo Supabase Auth e mantém as credenciais.
- `public.app_user.auth_user_id` referencia `auth.users.id`; a tabela interna não replica senha, hash ou credencial.
- Papéis e permissões do MaterialCRM são próprios do domínio comercial e não serão confiados a metadados editáveis pelo usuário.
- Relações compostas garantem que usuários, papéis e filiais associados pertençam à mesma empresa.
- O backend valida a empresa e a filial autorizadas em cada operação.

## Convenções

- Entidades persistentes terão identificador UUID.
- Datas de evento serão armazenadas com fuso horário.
- Tabelas transacionais mantêm contexto organizacional quando aplicável.
- Exclusão lógica será avaliada por entidade; documentos e transações concluídas devem ser preservados.
- Alterações de schema serão versionadas exclusivamente pelo Supabase CLI.
