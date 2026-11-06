# Missão `availability-query`: consulta de horários livres

Calcule, com uma consulta SQL, os horários livres de um profissional em um dia.

Testes: `src/test/java/io/github/precavidos/clinic/missions/AvailabilityQueryTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=availability-query`.

## Endpoint

`GET /professionals/{id}/availability?date=2030-03-04`

`200 OK` com a lista dos intervalos livres do dia, em ordem:

```json
[
  {"start": "2030-03-04T08:00:00", "end": "2030-03-04T09:00:00"},
  {"start": "2030-03-04T10:00:00", "end": "2030-03-04T18:00:00"}
]
```

## Regras

- O dia vai do horário de abertura ao de fechamento (`clinic.opening-hours`, das 08:00 às 18:00, de segunda a sexta, no fuso `clinic.time-zone`).
- Um intervalo livre é um trecho do dia sem consulta do profissional. Consultas vêm da tabela `appointments` (`starts_at` + `duration_minutes`).
- Consultas de outros profissionais e de outros dias não contam.
- Não há intervalos vazios: consultas encostadas não geram intervalo entre elas, e não há intervalo antes de uma consulta que começa na abertura nem depois de uma que termina no fechamento.
- Dia sem atendimento (sábado e domingo) devolve `[]`.
