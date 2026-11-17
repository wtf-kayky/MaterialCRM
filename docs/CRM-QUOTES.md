# Etapa 09 — CRM e orçamentos

## Fluxo inicial

O CRM inicia com oportunidades no funil `NEW → QUALIFIED → PROPOSAL → WON/LOST`. Os estados finais não são reabertos pelo endpoint inicial, preservando a rastreabilidade comercial. A escolha de estágios abertos e fechados segue o padrão de pipelines comerciais com resultados ganhos e perdidos documentado pelo HubSpot.

Orçamentos nascem em `DRAFT` e usam o preço vigente do catálogo no momento da criação. O endpoint não aceita preço informado pelo cliente, evitando manipulação de valores.

## Acesso e isolamento

- Oportunidades e orçamentos são sempre limitados à empresa e à filial ativa autorizada.
- Apenas `ADMIN`, `MANAGER` e `SELLER` podem criar oportunidades ou orçamentos e movimentar oportunidades.
- Cliente, oportunidade e produto são validados no mesmo escopo organizacional.
- A criação e a mudança de estágio registram eventos de auditoria.

## Endpoints

| Método | Rota | Finalidade |
| --- | --- | --- |
| `GET` | `/api/v1/opportunities` | lista o funil da filial ativa |
| `POST` | `/api/v1/opportunities` | cria oportunidade em `NEW` |
| `POST` | `/api/v1/opportunities/{opportunityId}/stage` | move para `QUALIFIED`, `PROPOSAL`, `WON` ou `LOST` |
| `GET` | `/api/v1/quotes` | lista orçamentos da filial ativa |
| `POST` | `/api/v1/quotes` | cria orçamento com itens do catálogo |

## Limites deliberados

Aceite, rejeição, expiração automática, conversão em venda e regras configuráveis de funil serão implementados em incrementos posteriores. Esta versão evita expor esses fluxos como se estivessem prontos.

## Referência

- [HubSpot — pipelines de objetos](https://knowledge.hubspot.com/object-settings/set-up-and-customize-pipelines): oportunidades de venda usam estágios e resultados fechados de ganho/perda.
