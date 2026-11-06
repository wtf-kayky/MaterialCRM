# Arquitetura técnica

## Objetivo

Entregar um CRM comercial para lojas de materiais de construção com operação local-first: vendas, estoque, clientes, financeiro e autenticação devem continuar disponíveis na rede LAN mesmo sem internet. A gestão comercial das assinaturas do produto será feita em um ambiente central separado.

## Dois ambientes de produção

### 1. MaterialCRM Admin — ambiente central hospedado

Aplicação exclusiva do fornecedor do MaterialCRM para:
- cadastrar empresas clientes e instalações;
- criar planos, preços, limites e períodos de teste;
- acompanhar assinaturas, pagamentos, vencimentos e status;
- emitir licenças digitais assinadas;
- receber telemetria operacional mínima e sincronizações autorizadas;
- administrar suspensões e renovações.

Este ambiente terá sua própria API e banco de dados. Não será dependência para o funcionamento diário do CRM local.

### 2. MaterialCRM Local — ambiente instalado em cada cliente

Cada loja terá um servidor local (computador dedicado ou mini-PC) executando por Docker Compose:
- frontend React/TypeScript servido por Nginx;
- API Spring Boot;
- PostgreSQL local com volume persistente;
- autenticação e autorização locais;
- agente de sincronização com o MaterialCRM Admin.

Os computadores da loja acessam o sistema pelo navegador usando o endereço LAN do servidor, por exemplo `http://192.168.0.100:8080`. No próprio servidor, também será possível acessar por `http://localhost:8080`.

## Diagrama lógico

```text
                  INTERNET (quando disponível)
                              |
                   HTTPS / sincronização
                              |
             +-------------------------------+
             |       MaterialCRM Admin      |
             | API central + PostgreSQL     |
             | Clientes, planos, pagamentos |
             | Licenças e gestão de acesso  |
             +-------------------------------+
                              ^
                              | sincronização segura
                              | (fila/outbox e retries)
                              v
        +---------------------------------------------+
        |           SERVIDOR LOCAL DA LOJA            |
        |                                             |
        |  Nginx / React -> Spring Boot API           |
        |                       |                     |
        |                 PostgreSQL local            |
        |                 volume persistente          |
        |                       |                     |
        |               autenticação local           |
        +---------------------------------------------+
                    |              |
                 LAN PC 1       LAN PC 2
```

## Princípios obrigatórios

1. O ambiente local é a fonte de verdade das operações da loja.
2. Internet não é necessária para login local nem para operações comerciais do dia a dia.
3. O Admin central não participa do caminho síncrono de vendas, estoque, clientes ou caixa.
4. Cada instalação mantém seus próprios dados e identidade de instalação.
5. Toda sincronização é assíncrona, idempotente, auditável e retomável após falhas.
6. Conflitos de sincronização não podem sobrescrever silenciosamente dados operacionais.
7. Dados de empresas diferentes são isolados no Admin e no backend local.
8. O backend aplica autorização por empresa, filial, papel e permissão; a interface não é uma barreira de segurança.
9. O sistema deve permitir backup e restauração locais sem depender da nuvem.
10. O monólito modular continua sendo a arquitetura inicial; Admin e Local são produtos implantáveis separados, com contratos de integração versionados.

## Licenciamento offline

- O Admin emite uma licença assinada digitalmente com identificador da instalação, plano, recursos e datas relevantes.
- A chave privada de assinatura fica exclusivamente protegida no ambiente Admin.
- O MaterialCRM Local contém somente a chave pública para verificar autenticidade e integridade da licença.
- A instalação armazena a última licença válida e o último estado de sincronização.
- A expiração durante uma interrupção de internet não bloqueia o funcionamento local. O sistema continua operando e marca a licença como aguardando revalidação.
- Quando a conexão retorna, a instalação sincroniza o estado da assinatura e recebe atualizações administrativas.
- Uma suspensão ou alteração remota não pode alcançar uma instalação totalmente desconectada até que ela volte a se comunicar. Isso deve ser apresentado claramente no painel Admin.
- A política de funcionamento após uma revalidação online que informe suspensão será definida antes da implementação do bloqueio comercial. Nunca se deve apagar ou bloquear acesso aos dados da loja.

## Sincronização e integridade

O primeiro escopo de sincronização será deliberadamente limitado a dados administrativos e de licença. Para dados operacionais, a arquitetura prevê:
- identificadores UUID gerados localmente;
- tabela outbox para eventos pendentes;
- confirmação de recebimento e chaves de idempotência;
- retries com backoff;
- versão de contrato de eventos;
- trilha de auditoria e diagnóstico de última sincronização.

A sincronização de vendas, estoque e financeiro entre filiais em locais geográficos diferentes exige regras adicionais de conflito e consistência. Não será presumida como automática no primeiro lançamento.

## Persistência e migrations

- MaterialCRM Local terá PostgreSQL local como banco operacional.
- MaterialCRM Admin terá banco próprio, separado do banco de cada loja.
- Cada ambiente terá migrations versionadas e uma única ferramenta de migrations por aplicação.
- O schema Supabase já criado será preservado como artefato existente e poderá apoiar o desenho do Admin, mas não será considerado automaticamente o banco operacional local.
- A escolha final da ferramenta de migrations local/central será registrada antes da implementação do banco.

## Segurança

- Senhas de usuários da loja serão armazenadas somente como hashes seguros no banco local.
- O provisionamento inicial do administrador local será feito por fluxo seguro de primeiro início, sem credenciais fixas no repositório.
- Tokens e segredos serão guardados no servidor e em variáveis de ambiente protegidas.
- A licença será verificada criptograficamente no backend local; o frontend não decide o estado da licença.
- O Admin e cada instalação terão credenciais próprias de sincronização, com rotação e revogação.
- Todas as rotas de negócio validam usuário, empresa, filial e permissão no backend.
- O tráfego externo de sincronização usará HTTPS.
- Backups locais criptografados e testes periódicos de restauração são requisitos de produção.

## Implantação

- Admin: implantação hospedada com domínio e HTTPS, disponibilidade e backups monitorados.
- Local: Docker Compose no servidor de cada cliente, com volume PostgreSQL persistente e acesso LAN.
- O acesso externo à loja não será exposto diretamente à internet por padrão.
- Atualizações do Local devem poder ser instaladas por pacote/imagem previamente baixado; o procedimento de atualização offline será documentado.
- Internet pode ser necessária para instalação inicial, obtenção de imagens e atualizações, mas não para operação normal depois de instalado.

## Decisões registradas

| Tema | Diretriz |
| --- | --- |
| Ambientes de produção | Admin central hospedado + Local em cada loja |
| Operação comercial | Local-first, pela LAN |
| Banco operacional da loja | PostgreSQL local |
| Gestão de assinatura | MaterialCRM Admin |
| Licença offline | Assinatura digital validada localmente |
| Vencimento sem internet | Não interrompe a operação local |
| Sincronização | Assíncrona, retomável e idempotente |
| Backend | Spring Boot modular |
| Frontend | React + TypeScript |
| Contêineres | Docker Compose no ambiente Local |
| Multiunidade | Empresa e filial no contexto de autorização |
| Integrações | Contratos REST/eventos versionados |
