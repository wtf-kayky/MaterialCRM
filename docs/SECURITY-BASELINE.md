# Base de segurança

O MaterialCRM adotará OWASP ASVS nível 2 como referência para uma aplicação comercial que processa dados de clientes, vendas e pagamentos.

## Separação de ambientes

- MaterialCRM Admin e MaterialCRM Local são ambientes de produção distintos.
- O Admin não autentica funcionários da loja nem participa de transações comerciais locais.
- Cada instalação Local possui identidade de instalação e credenciais de sincronização próprias.
- A indisponibilidade do Admin não pode comprometer a segurança nem a disponibilidade das operações locais.

## Identidade e autenticação local

- A API local será a autoridade de credenciais e sessões dos funcionários.
- Senhas serão armazenadas apenas como hashes seguros, usando algoritmo apropriado para senhas e parâmetros atualizáveis.
- O primeiro administrador será provisionado por fluxo seguro, sem credenciais padrão no código.
- Funcionários serão provisionados por administrador autorizado; não haverá cadastro público com associação automática a uma empresa.
- Sessões e tokens terão expiração, rotação/revogação e proteção contra abuso.

## Licenças e sincronização

- O Admin assina licenças digitalmente; a chave privada não será distribuída às instalações.
- O Local verifica licença usando chave pública e não depende de consulta online para validar a assinatura.
- Segredos de sincronização são distintos por instalação, protegidos e revogáveis.
- Comunicação externa usa HTTPS.
- Dados enviados ao Admin são minimizados e auditáveis.
- Falhas e retries de sincronização não podem duplicar operações nem bloquear o caixa.
- Uma instalação desconectada não recebe suspensões remotas até reconectar; o Admin deve exibir a última comunicação conhecida.

## Autorização e isolamento

- O backend valida permissão, empresa e filial em toda operação protegida.
- O frontend não é uma fronteira de segurança.
- Toda entidade operacional possui contexto de empresa e, quando aplicável, filial.
- Tentativas de acesso cruzado entre empresas e filiais devem ser rejeitadas por testes.

## Outros controles obrigatórios

- Privilégio mínimo para usuários e serviços.
- Registros de auditoria para ações administrativas e transacionais, protegidos contra alteração indevida.
- Validação de entrada e respostas de erro sem detalhes internos.
- Segredos fornecidos por ambiente, nunca versionados.
- Dependências com versões controladas e revisão regular de vulnerabilidades.
- Banco PostgreSQL não exposto diretamente à internet.
- Backups locais protegidos e testes periódicos de restauração.
- Atualizações de software autenticadas e com procedimento de rollback.

## Revisão antes de produção

1. Testar autenticação e autorização local sem internet.
2. Testar tentativas de acesso entre empresas e filiais.
3. Testar assinatura, expiração offline e sincronização da licença.
4. Testar perda e retorno de conexão durante operações comerciais.
5. Testar backup, restauração e recuperação após falha.
6. Revisar segredos, dependências, cabeçalhos e erros expostos.
