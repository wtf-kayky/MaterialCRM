# Execução local planejada

## Topologia prevista

O servidor local da empresa executará PostgreSQL, backend e frontend por Docker Compose. Navegadores dos computadores de balcão, caixa, estoque e gerência acessarão o frontend pela rede LAN.

```text
Computadores na LAN ──► Frontend ──► API ──► PostgreSQL
```

## Serviços planejados

| Serviço | Responsabilidade | Persistência |
| --- | --- | --- |
| `frontend` | Interface React servida localmente | Não aplicável |
| `backend` | API e regras de negócio | Não aplicável |
| `postgres` | Dados operacionais | Volume local dedicado |

## Variáveis necessárias

O arquivo `.env` local, nunca versionado, fornecerá ao menos:

- credenciais do PostgreSQL;
- nome do banco;
- segredo de assinatura de sessão;
- endereço e porta públicos na LAN;
- configuração de backup.

## Critério para a Etapa 02

A fundação técnica estará pronta quando Docker Compose iniciar os três serviços, a API responder a uma verificação de saúde e o frontend conseguir consumir a API localmente.

