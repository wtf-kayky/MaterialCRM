# Auditoria inicial de segurança — 2026-11-06

## Escopo

Revisão estática da fundação técnica e do controle de acesso da Etapa 04.

## Controles verificados

| Controle | Estado | Evidência |
| --- | --- | --- |
| Rotas fechadas por padrão | Aplicado | Configuração de segurança exige autenticação, exceto health check e login |
| Sessão sem estado | Aplicado | API usa JWT Bearer e política de sessão sem estado |
| Senhas com hash forte | Aplicado | BCrypt com custo 12 no provisionamento e na administração de usuários |
| Segredo de token | Aplicado com validação | Chave HMAC vem de variável de ambiente e exige ao menos 32 bytes |
| Isolamento por empresa | Aplicado nos endpoints atuais | Sessão, empresa e auditoria filtram pelo usuário autenticado |
| Acesso por filial de clientes | Aplicado | Consultas e alterações de clientes exigem vínculo explícito do usuário com a filial |
| Separação administrativa | Aplicado | Gerentes não podem criar, promover, desativar ou alterar administradores |
| Dados sensíveis em repositório | Não encontrados | Credenciais são lidas exclusivamente por variáveis de ambiente |

## Pendências antes de produção

- Estender a validação de acesso por filial aos módulos de produtos, estoque, vendas e relatórios.
- Adicionar limitação de tentativas de login e eventos de falha.
- Implementar renovação e revogação controladas de tokens.
- Incluir testes de autorização entre empresas, filiais e perfis.
- Fixar versões de dependências e executar análise de vulnerabilidades em pipeline.
- Configurar TLS na implantação em rede local.

Nenhuma pendência acima autoriza liberar o sistema em produção. A revisão será atualizada conforme os módulos forem adicionados.
