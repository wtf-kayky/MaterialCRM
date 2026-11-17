# Etapa 10 — Dashboard operacional

O resumo operacional consolida exclusivamente dados da empresa e da filial ativa autorizada ao usuário. Ele não aceita identificadores de empresa ou filial na rota, reduzindo o risco de acesso horizontal indevido.

## Endpoint

`GET /api/v1/dashboard/summary?from=YYYY-MM-DD&to=YYYY-MM-DD`

Somente `ADMIN` e `MANAGER` com filial ativa autorizada podem consultar o endpoint.

## Indicadores

- Vendas concluídas no período: total e quantidade.
- Financeiro em aberto: valores a receber e a pagar.
- Estoque: quantidade de itens abaixo do mínimo na filial.
- CRM: contagem de oportunidades por estágio.

## Regras

- Sem parâmetros, o período é do primeiro dia do mês corrente até hoje.
- O intervalo máximo é de 366 dias; datas invertidas são rejeitadas.
- Indicadores financeiros e comerciais permanecem segregados pela filial ativa.

Relatórios exportáveis, indicadores entre filiais e séries históricas agregadas serão adicionados após definição de políticas de visibilidade e retenção.
