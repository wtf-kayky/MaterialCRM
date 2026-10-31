# Conectar GitHub ao Supabase

O projeto já tem uma pasta `supabase/` versionada e a migration inicial aplicada no projeto Cloud. A integração nativa GitHub ↔ Supabase ainda exige autorização OAuth pelo proprietário do repositório; essa autorização precisa ser concluída no painel.

## Configuração única no Dashboard

1. Abra [Project Settings → Integrations](https://supabase.com/dashboard/project/hmkkzlstjpnsfrwqcjax/settings/integrations).
2. Na seção **GitHub Integration**, clique em **Authorize GitHub**.
3. No GitHub, autorize o aplicativo Supabase e conceda acesso ao repositório `wtf-kayky/MaterialCRM`.
4. Volte ao Dashboard e selecione esse repositório.
5. Em **Working directory**, informe `.`, pois a pasta `supabase/` está na raiz.
6. Confira qual branch está configurada como branch de produção. Durante o desenvolvimento inicial, use a branch de trabalho aprovada para este ambiente; não habilite deploy automático da `main` até que as alterações sejam revisadas e mescladas.
7. Habilite a integração e confira o status do repositório.

## Como a integração funciona

- O Supabase observa commits, branches e pull requests do repositório.
- As migrations em `supabase/migrations` são aplicadas conforme a configuração de deploy.
- Deploy para produção deve ocorrer somente após revisão e merge para a branch de produção escolhida.
- Preview branches automáticas dependem do plano Supabase com Branching habilitado.
- A integração não substitui testes locais nem revisão de migrations.

## Convenção do MaterialCRM

- A fonte de verdade do schema é `supabase/migrations`.
- Toda mudança de banco começa por uma migration revisada e versionada.
- Nunca alterar o schema diretamente no Dashboard sem depois registrar a mudança no repositório.
- Não guardar senhas, tokens de acesso, chaves secretas ou connection strings no GitHub.

## Estado atual

- [x] Repositório GitHub contém a configuração e as migrations Supabase.
- [x] Projeto Cloud recebeu a estrutura inicial e os índices complementares.
- [ ] Autorização OAuth da integração GitHub concluída pelo proprietário.
- [ ] Repositório selecionado no Dashboard e working directory configurado como `.`.
- [ ] Deploy automático habilitado somente após confirmar a branch de destino.
