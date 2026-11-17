# ADR 002 — Dois ambientes de produção e operação local-first

- **Status:** Aprovado
- **Etapa:** 01 — Fundação e decisões arquiteturais
- **Data:** 2026-10-31
- **Substitui:** ADR 001 como arquitetura de runtime do produto Local

## Contexto

O MaterialCRM precisa atender lojas que não podem parar quando a conexão com a internet falhar. Ao mesmo tempo, o fornecedor precisa administrar planos, clientes, pagamentos, assinaturas e licenças de forma centralizada.

A arquitetura anterior colocava banco de dados e autenticação no Supabase Cloud, tornando login e operações dependentes da internet. Isso não atende ao requisito offline-first.

## Decisão

O produto será composto por dois ambientes de produção separados:

1. **MaterialCRM Admin:** aplicação central hospedada, com banco próprio, para gestão do fornecedor, assinaturas, pagamentos, emissão de licenças e acompanhamento das instalações.
2. **MaterialCRM Local:** aplicação instalada em cada cliente, com frontend, API, autenticação e PostgreSQL locais. O tráfego comercial não depende do Admin nem da internet.

O Admin e o Local se comunicam por sincronização assíncrona via HTTPS. A sincronização não participa de transações de vendas, estoque, clientes ou financeiro.

## Política de licença offline

- O Admin emite licenças com assinatura digital.
- A chave privada permanece somente no ambiente central.
- O Local valida a licença usando uma chave pública distribuída com segurança.
- A licença e o último estado conhecido ficam armazenados localmente.
- Se a licença vencer enquanto a instalação estiver offline, o CRM continua operando; o estado é marcado como aguardando revalidação.
- Quando a conexão retornar, o Local consulta e recebe o estado atualizado.
- Suspensões remotas não são instantâneas em instalações desconectadas.
- A política de acesso após uma revalidação online que retorne status suspenso será definida antes de implementar bloqueios. Os dados do cliente nunca serão apagados por mudança de assinatura.

## Persistência e migrations

- Cada instalação Local terá banco PostgreSQL próprio.
- O Admin terá banco separado.
- Cada aplicação terá uma estratégia única e versionada de migrations.
- O schema já criado no Supabase será preservado, sem exclusão ou migração destrutiva nesta decisão.
- O uso futuro do Supabase como hospedagem de banco do Admin ou como destino de backup/sincronização será uma decisão separada; não é requisito do Local.

## Consequências

- A operação comercial permanece disponível sem internet, desde que servidor local, energia e LAN estejam disponíveis.
- Cada cliente precisa de servidor local, armazenamento e rotina de backup.
- O Admin não consegue aplicar uma alteração em tempo real numa instalação desconectada.
- Sincronização entre filiais geograficamente separadas requer desenho específico de conflitos e consistência.
- Atualizações de software e imagens Docker precisam de estratégia para ambientes sem internet.
- O modelo de identidade do Admin é independente do cadastro de usuários locais.

## Critérios de aceite

- O Local inicia e autentica usuários sem acesso à internet.
- Operações comerciais não fazem chamadas síncronas ao Admin.
- Dados persistem após reinício dos contêineres.
- Uma licença vencida durante desconexão não interrompe a operação.
- A instalação sincroniza e atualiza seu estado ao recuperar conexão.
- O Admin identifica instalações sem comunicação e exibe a última sincronização conhecida.
- Backup e restauração locais são documentados e testados.
