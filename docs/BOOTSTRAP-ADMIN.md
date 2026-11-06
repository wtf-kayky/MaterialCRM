# Provisionamento do administrador inicial

O primeiro administrador é criado apenas quando a base não possui usuários e as quatro variáveis abaixo estão preenchidas no ambiente do backend:

- `MATERIALCRM_BOOTSTRAP_COMPANY_NAME`
- `MATERIALCRM_BOOTSTRAP_COMPANY_TAX_ID`
- `MATERIALCRM_BOOTSTRAP_ADMIN_EMAIL`
- `MATERIALCRM_BOOTSTRAP_ADMIN_PASSWORD`

A senha nunca deve ser versionada. Após o primeiro início bem-sucedido, remova `MATERIALCRM_BOOTSTRAP_ADMIN_PASSWORD` do ambiente. O provisionamento é ignorado quando já existe um usuário, evitando sobrescrita ou recriação acidental.

O administrador recebe acesso à filial Matriz e às permissões administrativas iniciais. A criação é registrada na auditoria.
