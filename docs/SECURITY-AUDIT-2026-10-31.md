# Auditoria inicial de segurança — 2026-11-17

## Escopo

Revisão estática da fundação técnica e do controle de acesso das Etapas 04 a 07, incluindo o registro de eventos de autenticação.

## Controles verificados

| Controle | Estado | Evidência |
| --- | --- | --- |
| Rotas fechadas por padrão | Aplicado | Configuração de segurança exige autenticação, exceto health check e login |
| Sessão sem estado | Aplicado | API usa JWT Bearer e política de sessão sem estado |
| Senhas com hash forte | Aplicado | BCrypt com custo 12 no provisionamento e na administração de usuários |
| Segredo de token | Aplicado com validação | Chave HMAC vem de variável de ambiente e exige ao menos 32 bytes |
| Limite de tentativas de login | Aplicado para a instância local | Cinco falhas por e-mail e endereço remoto em 15 minutos retornam HTTP 429 |
| Auditoria de autenticação | Aplicado | Sucessos e falhas vinculáveis a uma conta são registrados sem senha, token ou endereço remoto |
| Isolamento por empresa | Aplicado nos endpoints atuais | Sessão, empresa e auditoria filtram pelo usuário autenticado |
| Acesso por filial de clientes | Aplicado | Consultas e alterações de clientes exigem vínculo explícito do usuário com a filial |
| Acesso ao estoque | Aplicado | Filial ativa exige vínculo explícito e movimentações são restritas a perfis autorizados |
| Consistência de estoque | Aplicado | Movimentações executam em transação e bloqueiam o saldo existente durante o cálculo |
| Acesso a vendas | Aplicado | Vendas exigem filial autorizada, perfil comercial permitido e cliente da mesma filial |
| Consistência de vendas | Aplicado | A baixa do estoque ocorre dentro da transação da venda e bloqueia o saldo consultado |
| Separação administrativa | Aplicado | Gerentes não podem criar, promover, desativar ou alterar administradores |
| Dados sensíveis em repositório | Não encontrados | Credenciais são lidas exclusivamente por variáveis de ambiente |

## Decisão de auditoria

Falhas de autenticação sem conta correspondente não entram na tabela de auditoria: não há empresa confiável para associá-las e registrar o e-mail informado criaria retenção desnecessária de dado pessoal. Elas continuam sujeitas ao limitador de tentativas. Falhas com conta conhecida e sucessos são vinculados ao usuário e à empresa.

## Pendências antes de produção

- Migrar o limite de tentativas para um mecanismo compartilhado caso haja mais de uma instância da API.
- Implementar renovação e revogação controladas de tokens.
- Incluir testes de autorização entre empresas, filiais e perfis.
- Fixar versões de dependências e executar análise de vulnerabilidades em pipeline.
- Configurar TLS na implantação em rede local.

Nenhuma pendência acima autoriza liberar o sistema em produção. A revisão será atualizada conforme os módulos forem adicionados.
