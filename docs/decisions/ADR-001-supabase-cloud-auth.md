# ADR 001 — Supabase Cloud como banco e autenticação (decisão substituída)

- **Status:** Substituído pelo ADR 002
- **Etapa:** 01 — Fundação e decisões arquiteturais
- **Data:** 2026-10-31

## Histórico

Esta decisão registrou a primeira proposta técnica: frontend e API locais, com PostgreSQL e Supabase Auth na nuvem. Ela foi implementada parcialmente na branch `stage-01-planning-foundation`, incluindo schema inicial no projeto Supabase.

## Motivo da substituição

O requisito de produto foi esclarecido: as lojas precisam continuar trabalhando mesmo sem internet. A arquitetura Supabase Cloud como banco operacional e autoridade de login exige conectividade e, portanto, não atende ao requisito.

## Situação dos artefatos existentes

- O projeto Supabase e as migrations já criadas devem ser preservados.
- Nenhum schema, tabela ou dado será apagado por esta decisão.
- O código e a configuração que dependem do Supabase serão migrados gradualmente para a arquitetura local-first.
- O uso futuro do Supabase como serviço opcional do Admin, backup ou sincronização exige decisão própria e não é dependência do MaterialCRM Local.

## Decisão vigente

Consulte [ADR 002 — Dois ambientes de produção e operação local-first](ADR-002-admin-and-local-offline-first.md).
