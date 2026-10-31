# Etapa 04 — Empresas, filiais e usuários

## Progresso

O backend agora fecha todas as rotas por padrão, exceto a verificação de saúde. Esta política restritiva permanece até que o login por token e o provisionamento do primeiro administrador estejam concluídos.

## Modelo de autorização

Uma identidade pertence a uma empresa, recebe um ou mais perfis, herda permissões do perfil e recebe acesso explícito às filiais autorizadas. A autorização final de cada operação deve validar usuário ativo, permissão, empresa e filial.

## Próximas entregas

1. Provisionamento seguro do primeiro administrador por variável de ambiente de uso único.
2. Login com sessão de curta duração e renovação controlada.
3. API de empresas, filiais, usuários, perfis e auditoria.
4. Integração da tela de login e seleção de filial.

## Controles aplicados

- Senhas devem usar BCrypt.
- Sessões HTTP no servidor estão desativadas.
- Rotas ficam fechadas por padrão.
- Cabeçalhos de proteção incluem política de conteúdo e proibição de frame.
