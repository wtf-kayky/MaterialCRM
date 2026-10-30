# Base de segurança

O MaterialCRM adotará o OWASP ASVS nível 2 como referência para uma aplicação comercial que processa dados de clientes, vendas e pagamentos.

## Controles obrigatórios

- Autenticação no backend e senhas armazenadas exclusivamente como hashes fortes.
- Autorização por permissão, empresa e filial em toda operação protegida.
- Privilégio mínimo: usuários não recebem acesso por padrão.
- Registros de auditoria somente por acréscimo para ações administrativas e transacionais.
- Validação de entrada e respostas de erro sem detalhes internos.
- Segredos fornecidos por ambiente, nunca versionados.
- Dependências com versões controladas e revisão regular de vulnerabilidades.

## Isolamento de dados

Toda entidade operacional deverá possuir contexto de empresa e, quando aplicável, filial. O backend validará esse contexto a partir do usuário autenticado; o frontend não é uma fronteira de segurança.

## Revisão antes de integrar

1. Verificar autenticação, sessão e expiração de tokens.
2. Testar tentativas de acesso entre empresas e filiais.
3. Revisar registros de auditoria para alterações de acesso.
4. Conferir variáveis de ambiente, cabeçalhos HTTP e erros expostos.
5. Executar análise de dependências e testes automatizados.

