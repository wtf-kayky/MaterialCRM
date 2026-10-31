# Base de segurança

O MaterialCRM adotará o OWASP ASVS nível 2 como referência para uma aplicação comercial que processa dados de clientes, vendas e pagamentos.

## Identidade e autenticação

- Supabase Auth será a autoridade de credenciais e sessões.
- A tabela `public.app_user` guardará o perfil de negócio e o vínculo com a identidade de `auth.users`; não armazenará senha ou hash de senha.
- A API deverá validar criptograficamente o JWT, incluindo emissor, assinatura e expiração, antes de confiar na identidade.
- Funcionários serão provisionados por convite ou administrador autorizado; não haverá cadastro público com associação automática a uma empresa.

## Autorização e isolamento de dados

- O backend validará permissão, empresa e filial em toda operação protegida.
- O frontend não é uma fronteira de segurança e não consulta diretamente as tabelas operacionais.
- Toda entidade operacional deverá possuir contexto de empresa e, quando aplicável, filial.
- As tabelas iniciais em `public` são criadas com RLS habilitado e sem políticas de acesso direto; `anon` e `authenticated` não recebem privilégios nessas tabelas.
- O backend usa JDBC. RLS não deve ser presumido como proteção das consultas executadas por uma conexão privilegiada que possa ignorá-la. Antes de produção, será definido e testado um papel de banco de menor privilégio e o contexto seguro de empresa/filial.

## Outros controles obrigatórios

- Privilégio mínimo: usuários não recebem acesso por padrão.
- Registros de auditoria para ações administrativas e transacionais, protegidos contra alteração indevida.
- Validação de entrada e respostas de erro sem detalhes internos.
- Segredos fornecidos por ambiente, nunca versionados.
- Dependências com versões controladas e revisão regular de vulnerabilidades.
- TLS nas conexões com o PostgreSQL gerenciado.

## Revisão antes de integrar

1. Verificar autenticação, sessão e expiração de tokens.
2. Testar tentativas de acesso entre empresas e filiais.
3. Revisar registros de auditoria para alterações de acesso.
4. Conferir variáveis de ambiente, cabeçalhos HTTP e erros expostos.
5. Executar análise de dependências e testes automatizados.
