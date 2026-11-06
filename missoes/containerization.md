# Missão `containerization`: aplicação em contêiner

Empacote a aplicação em uma imagem Docker e suba tudo com Docker Compose.

Testes: `src/test/java/io/github/precavidos/clinic/missions/ContainerizationTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=containerization`. O primeiro teste constrói a imagem e pode levar alguns minutos (o limite é de 10 minutos).

## `Dockerfile`, na raiz do projeto

- O contexto do build é a raiz do projeto. A imagem precisa ser construída só a partir do código-fonte: o empacotamento da aplicação acontece dentro do `docker build`, porque o teste roda antes de existir um `target/*.jar`.
- A aplicação escuta na porta `8080` e responde `200` em `/actuator/health`.
- A conexão com o banco vem das variáveis de ambiente `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`. O teste usa `jdbc:postgresql://db:5432/clinic`, usuário `clinic` e senha `clinic`.
- Sugestão: baixe as dependências do Maven numa camada separada do código-fonte, assim o Docker reaproveita o cache quando só o código muda. Um `.dockerignore` que exclua `target/` também deixa o build mais rápido.

## `compose.yaml`, na raiz do projeto

- Um serviço para a aplicação, com `build` apontando para o `Dockerfile` e a porta `8080` publicada em `ports`.
- Um serviço para o banco com a imagem `postgres:17` (ou uma variante, como `postgres:17-alpine`).
- No serviço da aplicação, `environment` define `SPRING_DATASOURCE_URL` apontando para o serviço do banco pelo nome dele (por exemplo, `jdbc:postgresql://db:5432/clinic`), além de usuário e senha.
