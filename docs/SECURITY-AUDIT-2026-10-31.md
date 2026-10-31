# Auditoria inicial de segurança — 2026-10-31

## Escopo

Revisão estática da fundação técnica e do controle de acesso da Etapa 04.

## Controles verificados

| Controle | Estado | Evidência |
| --- | --- | --- |
| Rotas fechadas por padrão | Aplicado | Configuração de segurança exige autenticação, exceto health check |
| Senhas com hash forte | Aplicado | BCrypt no provisionamento inicial |
| Isolamento por empresa | Aplicado nos endpoints atuais | Sessão, empresa e auditoria filtram pelo usuário autenticado |
| Acesso por filial | Modelado | Tabela de acesso explícito por filial e sessão lista filiais autorizadas |
| Auditoria de login | Aplicado | Evento de autenticação bem-sucedida cria registro de auditoria |
| Dados sensíveis em repositório | Não encontrados | Credenciais são lidas exclusivamente por variáveis de ambiente |

## Pendências antes de produção

- Substituir autenticação HTTP Basic por login com token de curta duração e renovação controlada.
- Adicionar limitação de tentativas de login e eventos de falha.
- Incluir testes de autorização entre empresas, filiais e permissões.
- Fixar versões de dependências e executar análise de vulnerabilidades em pipeline.
- Configurar TLS na implantação em rede local.

Nenhuma pendência acima autoriza liberar o sistema em produção. A revisão será atualizada conforme os módulos forem adicionados.
