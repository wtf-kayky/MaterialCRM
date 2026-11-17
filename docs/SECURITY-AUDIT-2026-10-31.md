# Auditoria inicial de segurança — 2026-11-17

## Escopo

Revisão estática da fundação técnica e dos módulos operacionais das Etapas 04 a 10.

## Controles verificados

| Controle | Estado | Evidência |
| --- | --- | --- |
| Rotas fechadas por padrão | Aplicado | Configuração de segurança exige autenticação, exceto health check e login |
| Sessão sem estado | Aplicado | API usa JWT Bearer e política de sessão sem estado |
| Senhas com hash forte | Aplicado | BCrypt com custo 12 no provisionamento e na administração de usuários |
| Segredo de token | Aplicado com validação | Chave HMAC vem de variável de ambiente e exige ao menos 32 bytes |
| Limite de tentativas de login | Aplicado para a instância local | Cinco falhas por e-mail e endereço remoto em 15 minutos retornam HTTP 429 |
| Auditoria de autenticação | Aplicado | Sucessos e falhas vinculáveis a uma conta são registrados sem senha, token ou endereço remoto |
| Isolamento por empresa e filial | Aplicado nos módulos atuais | Operações usam empresa e filial ativas autorizadas do usuário, não identificadores enviados pelo cliente |
| Financeiro | Aplicado | Lançamentos são imutáveis nesta etapa; baixa é transacional e auditada |
| CRM e orçamentos | Aplicado | Cliente, oportunidade e produto são validados na mesma empresa e filial; preço do orçamento vem do catálogo |
| Dashboard | Aplicado | Indicadores exigem perfil administrativo e são filtrados por empresa e filial autorizadas |
| Acesso ao estoque e vendas | Aplicado | Filial autorizada, perfis permitidos e transações protegem movimentação e baixa de estoque |
| Separação administrativa | Aplicado | Gerentes não podem criar, promover, desativar ou alterar administradores |
| Dados sensíveis em repositório | Não encontrados | Credenciais são lidas exclusivamente por variáveis de ambiente |

## Decisões de segurança

- Falhas de autenticação sem conta correspondente não entram na tabela de auditoria: não há empresa confiável para associá-las e registrar o e-mail informado criaria retenção desnecessária de dado pessoal. Elas continuam sujeitas ao limitador de tentativas.
- O dashboard não recebe empresa ou filial por parâmetro; o escopo deriva somente da identidade autenticada e do vínculo ativo.
- Não há atualização ou exclusão direta de lançamentos financeiros nesta etapa; reversões exigirão fluxo auditável próprio.

## Pendências antes de produção

- Migrar o limite de tentativas para um mecanismo compartilhado caso haja mais de uma instância da API.
- Implementar renovação e revogação controladas de tokens.
- Incluir testes de autorização entre empresas, filiais e perfis.
- Fixar versões de dependências e executar análise de vulnerabilidades em pipeline.
- Configurar TLS na implantação em rede local.

Nenhuma pendência acima autoriza liberar o sistema em produção. A revisão será atualizada conforme os módulos forem adicionados.
