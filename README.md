# Vaga Viva: modelo Java/Spring

Repositório-modelo do arquétipo `java-spring` do Vaga Viva: a API de um sistema fictício de agendamento de clínica, com pacientes, profissionais e consultas.

O projeto começa incompleto. Cada missão do seu plano tem testes automatizados que falham até você implementá-la. O contrato de cada missão está em `missoes/`.

## Tecnologias

Java 21, Spring Boot 4, Maven (pelo Maven Wrapper, `./mvnw`), PostgreSQL 17, Flyway, JPA/Hibernate, JUnit 5, Testcontainers e MockMvc.

## Pré-requisitos

- JDK 21.
- Docker em execução: os testes sobem um PostgreSQL com Testcontainers.

Não é preciso instalar o Maven: use o `./mvnw` (ou `mvnw.cmd` no Windows).

## Comandos

| Para | Rode |
| --- | --- |
| rodar todos os testes | `./mvnw verify` |
| rodar os testes de uma missão | `./mvnw test -Dgroups=<id-da-missão>`, por exemplo `./mvnw test -Dgroups=patients-api` |
| subir a aplicação | `./mvnw spring-boot:run`, com um PostgreSQL em `localhost:5432` (banco, usuário e senha `clinic`) |

Para subir um PostgreSQL local:

```sh
docker run --rm -p 5432:5432 -e POSTGRES_DB=clinic -e POSTGRES_USER=clinic -e POSTGRES_PASSWORD=clinic postgres:17-alpine
```

A aplicação responde em `http://localhost:8080/actuator/health`.

## Seu plano: `.vaga-viva/plan.json`

O Vaga Viva cria o arquivo `.vaga-viva/plan.json` no seu repositório com as missões que você escolheu:

```json
{
  "archetype": "java-spring",
  "domain": "clinic-scheduling",
  "missions": ["patients-api", "persistence", "scheduling-rules", "pagination", "availability-query", "containerization"]
}
```

Os testes das missões fora do plano aparecem como desativados (*skipped*), com o motivo, e não como falhas. Sem esse arquivo, os testes de todas as missões rodam. Os testes que não pertencem a nenhuma missão sempre rodam.

O workflow `.github/workflows/vaga-viva.yml` roda `./mvnw verify` a cada push e pull request e guarda os relatórios dos testes. Ele é do Vaga Viva: não o altere.

## Missões

| Missão | Contrato |
| --- | --- |
| `patients-api` | [Cadastro de pacientes](missoes/patients-api.md) |
| `persistence` | [Pacientes no banco de dados](missoes/persistence.md) |
| `scheduling-rules` | [Regras de agendamento](missoes/scheduling-rules.md) |
| `input-validation` | [Validação e mensagens de erro](missoes/input-validation.md) |
| `bug-hunt` | [Caça ao bug](missoes/bug-hunt.md) |
| `pagination` | [Listagem paginada de consultas](missoes/pagination.md) |
| `ci-pipeline` | [Pipeline de integração contínua](missoes/ci-pipeline.md) |
| `availability-query` | [Consulta de horários livres](missoes/availability-query.md) |
| `containerization` | [Aplicação em contêiner](missoes/containerization.md) |
| `reminder-strategies` | [Lembretes por e-mail e SMS](missoes/reminder-strategies.md) |
| `api-security` | [Proteção da API](missoes/api-security.md) |

Os testes de cada missão ficam em `src/test/java/io/github/precavidos/clinic/missions/`. Leia os testes e as mensagens de falha: elas dizem o que ainda falta.

## Estrutura

```
src/main/java/io/github/precavidos/clinic/
  appointment/    consultas (entidade, repositório e cálculo da duração)
  config/         ClinicProperties (horário de funcionamento e fuso) e o Clock
  professional/   profissionais
  reminder/       contratos de envio de lembretes
src/main/resources/
  application.yaml
  db/migration/   migrações Flyway
src/test/java/io/github/precavidos/clinic/
  missions/       testes das missões
  support/        infraestrutura dos testes (PostgreSQL, cliente HTTP, dados fictícios)
  vagaviva/       extensão do JUnit que lê o seu plano
```

Todos os dados do projeto são fictícios.

## Licença

[MIT](LICENSE).
