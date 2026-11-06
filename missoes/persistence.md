# Missão `persistence`: pacientes no banco de dados

Guarde os pacientes no PostgreSQL com JPA e crie o esquema com uma migração.

Testes: `src/test/java/io/github/precavidos/clinic/missions/PersistenceTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=persistence`.

## Contrato

- A API de pacientes (missão `patients-api`) continua igual, mas lê e grava no banco.
- Tabela `patients`, criada por uma **migração Flyway nova** em `src/main/resources/db/migration` (versão 2 ou maior; não altere a `V1`):

| Coluna | Conteúdo |
| --- | --- |
| `id` | identificador numérico, gerado pelo banco quando o `INSERT` não informa o valor |
| `name` | nome |
| `email` | e-mail |
| `phone` | telefone |
| `birth_date` | data de nascimento, tipo `DATE` |

- Quem cria e altera o esquema é o Flyway. A propriedade `spring.jpa.hibernate.ddl-auto` precisa continuar `validate` (ou `none`).
- Um paciente inserido direto na tabela, por SQL, precisa aparecer em `GET /patients/{id}`. Alterar e remover pela API precisa alterar e remover a linha da tabela.

A coluna `appointments.patient_id` fica **sem** chave estrangeira para `patients`: os testes de outras missões inserem consultas com pacientes que não estão na tabela.
