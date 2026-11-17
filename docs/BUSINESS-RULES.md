# Regras de negócio iniciais

## Contexto organizacional

1. Todo dado operacional pertence a uma empresa.
2. Estoque, caixa, venda e orçamento pertencem a uma filial.
3. Um usuário só acessa empresas, filiais e módulos que estejam em sua autorização ativa.

## Produtos e estoque

1. Produto possui uma unidade de medida obrigatória.
2. O saldo disponível é o saldo físico menos a quantidade reservada.
3. Ajustes e entradas não alteram o histórico: criam uma movimentação identificada.
4. Uma reserva precisa indicar filial, produto, quantidade, origem e situação.
5. Não é permitido concluir uma venda com quantidade indisponível, exceto em fluxo futuro autorizado de venda sob encomenda.

## Vendas e financeiro

1. Descontos exigem a permissão correspondente e devem registrar autor e motivo quando configurado.
2. Uma venda concluída gera itens de venda, movimentos de estoque e lançamentos financeiros de forma atômica.
3. Cancelamento ou devolução gera eventos reversos; registros concluídos não são apagados.
4. Pagamentos mistos devem totalizar o valor devido, considerando troco quando houver dinheiro.
5. Recebimentos parciais mantêm o saldo em aberto rastreável.

## Auditoria

1. Alterações administrativas e eventos transacionais relevantes registram usuário, data, contexto da filial e operação executada.
2. Dados de auditoria são somente anexados; não são editáveis pela interface operacional.

