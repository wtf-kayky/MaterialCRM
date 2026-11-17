# Etapa 02 — Fundação local-first e separação dos ambientes

## Objetivo

Implementar a base técnica dos dois ambientes de produção, começando pelo MaterialCRM Local. A operação da loja deve funcionar sem internet; o Admin central será desenvolvido como aplicação separada.

## Decisões já registradas

- MaterialCRM Admin central hospedado para gestão de clientes, planos, assinaturas, pagamentos e licenças.
- MaterialCRM Local instalado em cada loja, com frontend, API, autenticação e PostgreSQL locais.
- Comunicação Admin ↔ Local assíncrona, via HTTPS, sem dependência no fluxo transacional comercial.
- Licença digital verificável offline; vencimento durante desconexão não bloqueia a operação local.
- Dados e autenticação de cada loja permanecem locais.

Consulte [ADR 002](decisions/ADR-002-admin-and-local-offline-first.md) e [Arquitetura](ARCHITECTURE.md).

## Sequência de implementação

1. Substituir a topologia Compose legada por frontend + API + PostgreSQL local persistente.
2. Remover a dependência de runtime do Supabase Auth e do JWT JWKS remoto.
3. Implementar autenticação local segura, provisionamento inicial do administrador e autorização.
4. Criar migrations para o banco local e validar a inicialização vazia.
5. Implementar endpoint de saúde e verificações de dependência local.
6. Criar backup e restauração do PostgreSQL e testar persistência após reinício.
7. Validar acesso pela LAN e operação com a internet desconectada.
8. Definir contrato e identidade de instalação para sincronização com o Admin.
9. Implementar emissão/verificação de licença digital e comportamento offline.
10. Criar o esqueleto independente do MaterialCRM Admin.

## Critérios de aceite

- [ ] Docker Compose inicia PostgreSQL, API e frontend.
- [ ] PostgreSQL persiste dados após recriação de contêiner.
- [ ] Usuário local consegue autenticar sem internet.
- [ ] Endpoint protegido rejeita usuário não autenticado.
- [ ] Senhas são armazenadas com hash seguro.
- [ ] Permissões por empresa e filial são validadas no backend.
- [ ] Acesso pelo IP LAN funciona em outro computador.
- [ ] Vendas e operações locais não fazem chamadas síncronas ao Admin.
- [ ] A licença vencida durante desconexão não interrompe operações locais.
- [ ] Backup e restauração são testados.
- [ ] Sincronização é retomável e idempotente.
- [ ] Build frontend, build Maven e Docker Compose validados.

## Limites

O repositório contém uma base inicial construída para a arquitetura Supabase Cloud. Ela não é considerada pronta para produção offline. O código será migrado por etapas, preservando o schema Supabase existente e sem alterar a branch `main`.
