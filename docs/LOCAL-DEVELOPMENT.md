# Desenvolvimento local do MaterialCRM Local

## Topologia

O ambiente local executa frontend, API, autenticação e PostgreSQL no servidor da loja. A internet não faz parte do caminho das operações comerciais.

```text
Computadores da loja (LAN)
          |
          v
Nginx / React (porta 8080)
          |
          v
Spring Boot API
     |         |
     v         v
PostgreSQL   Autenticação local
     |
     +---- volume persistente

Internet (quando disponível) ---> sincronização HTTPS com MaterialCRM Admin
```

## Pré-requisitos

- Docker Engine e Docker Compose.
- Para desenvolvimento sem Docker: Node.js 22/npm e JDK 21/Maven 3.9+.
- Acesso à internet é necessário para baixar imagens/dependências na instalação inicial e para sincronizar com o Admin, mas não para operação normal depois de instalado.

## Endereços de acesso

- No próprio servidor: `http://localhost:8080`.
- Em outro computador da loja: `http://IP-DO-SERVIDOR:8080`, por exemplo `http://192.168.0.100:8080`.
- Reserve um IP no roteador ou configure endereço estático para o servidor. Libere no firewall somente as portas necessárias à LAN.
- Não exponha diretamente as portas do banco de dados à rede de usuários ou à internet.

## Banco e persistência

O PostgreSQL será executado como serviço do Docker Compose, com volume nomeado. Reiniciar ou recriar o contêiner não pode apagar os dados. A rotina de backup deverá gerar cópia externa e documentar restauração. Não considere o volume Docker como backup.

## Primeiro administrador local

O primeiro administrador será criado por um fluxo de bootstrap seguro. Não serão usados usuário/senha padrão embutidos no código ou no repositório. O fluxo deverá exigir segredo temporário fornecido no primeiro início e invalidá-lo após o cadastro.

## Autenticação

As credenciais e sessões dos funcionários serão gerenciadas localmente pela API. Senhas serão armazenadas apenas como hashes seguros. O login não poderá depender de Supabase Auth, JWKS ou qualquer serviço remoto.

## MaterialCRM Admin e sincronização

A instalação local terá uma identidade própria e uma fila local de sincronização. Quando houver internet, enviará dados administrativos e de licença ao Admin via HTTPS. Falhas de conexão serão repetidas posteriormente; elas nunca devem impedir vendas ou outras operações locais.

A licença é verificada no backend local por assinatura digital. A chave privada fica no Admin; a instalação recebe apenas a chave pública. Vencimento durante uma desconexão não bloqueia o CRM. A instalação atualiza seu estado quando a conexão retornar.

## Executar

Os comandos definitivos de `docker compose config`, `docker compose up --build -d`, inspeção de saúde, backup e restauração serão publicados após a implementação do compose local-first. Não use o compose legado com variáveis Supabase como procedimento de produção.

## Estado de validação

A arquitetura foi documentada, mas o ambiente ainda não foi validado em uma máquina local. Builds, autenticação local, persistência PostgreSQL, acesso LAN, modo sem internet e restauração de backup continuam como critérios pendentes.
