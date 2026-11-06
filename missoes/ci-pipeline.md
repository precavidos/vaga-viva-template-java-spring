# Missão `ci-pipeline`: pipeline de integração contínua

Rode o build e os testes a cada push com GitHub Actions.

Testes: `src/test/java/io/github/precavidos/clinic/missions/CiPipelineTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=ci-pipeline`.

## Contrato

Crie o arquivo `.github/workflows/ci.yml`. Os testes leem esse YAML e conferem que:

- ele dispara nos eventos `push` e `pull_request`;
- um passo usa `actions/setup-java` com `java-version: 21` e `cache: maven`;
- um passo roda `./mvnw verify` (opções como `-B` são permitidas).

O arquivo `.github/workflows/vaga-viva.yml` é do Vaga Viva e não faz parte da missão. Não o altere nem o use como resposta.
