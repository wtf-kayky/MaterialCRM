# Contratos iniciais da API

Base de URL prevista: `/api/v1`.

Os contratos abaixo são de planejamento. A especificação OpenAPI versionada será criada com o backend na Etapa 02.

## Convenções

- JSON em UTF-8.
- Identificadores externos em UUID.
- Datas no padrão ISO 8601.
- Respostas de erro com `code`, `message`, `status`, `traceId` e, quando aplicável, `fields`.
- Rotas autenticadas recebem o contexto organizacional pelo token e, quando necessário, pela filial selecionada.

## Recursos iniciais

| Método | Rota | Finalidade |
| --- | --- | --- |
| POST | `/auth/login` | Autenticar usuário |
| POST | `/auth/refresh` | Renovar sessão |
| GET | `/me` | Obter usuário e permissões atuais |
| GET/POST | `/companies` | Consultar/criar empresas |
| GET/POST | `/branches` | Consultar/criar filiais autorizadas |
| GET/POST | `/customers` | Consultar/criar clientes |
| GET/POST | `/products` | Consultar/criar produtos |
| GET | `/inventory/availability` | Consultar disponibilidade por filial |
| POST | `/sales` | Registrar venda |
| POST | `/quotes` | Criar orçamento |

## Exemplo de erro

```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "Quantidade indisponível para o produto solicitado.",
  "status": 422,
  "traceId": "uuid"
}
```

