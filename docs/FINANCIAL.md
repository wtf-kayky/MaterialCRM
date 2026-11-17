# Etapa 08 — Financeiro inicial

## Escopo entregue

O módulo inicial mantém lançamentos por empresa e filial, sem permitir alteração de valor, tipo ou origem depois da criação.

- `RECEIVABLE`: valor a receber.
- `PAYABLE`: valor a pagar.
- Estados: `OPEN`, `SETTLED` e `CANCELLED` (o cancelamento ficará disponível somente com fluxo de reversão auditável).
- A baixa é atômica e só aceita um lançamento em aberto.

## Fluxo e acesso

1. Administrador ou gerente cria o lançamento na filial ativa autorizada.
2. Caixa, gerente ou administrador baixa o lançamento.
3. A baixa registra data, responsável e evento de auditoria.

Os endpoints são sempre escopados por empresa e filial do token. Não há endpoint de alteração ou exclusão nesta etapa, evitando que valores financeiros já registrados sejam reescritos sem fluxo de reversão.

## Endpoints

| Método | Rota | Permissão |
| --- | --- | --- |
| `GET` | `/api/v1/financial-entries` | usuário com filial ativa autorizada |
| `POST` | `/api/v1/financial-entries` | `ADMIN` ou `MANAGER` |
| `POST` | `/api/v1/financial-entries/{entryId}/settle` | `ADMIN`, `MANAGER` ou `CASHIER` |

## Limites deliberados

Pagamentos parciais, conciliação bancária, vínculo automático de vendas e cancelamento com estorno serão tratados em incrementos posteriores. Esta etapa não simula esses comportamentos; ela rejeita a baixa de qualquer lançamento que não esteja aberto.
