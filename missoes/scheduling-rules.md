# Missão `scheduling-rules`: regras de agendamento

Agende consultas respeitando o horário de funcionamento, sem sobreposição e sem datas no passado.

Testes: `src/test/java/io/github/precavidos/clinic/missions/SchedulingRulesTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=scheduling-rules`.

## Horário de funcionamento

Configurado em `src/main/resources/application.yaml` e disponível em `ClinicProperties`:

| Propriedade | Valor |
| --- | --- |
| `clinic.time-zone` | `America/Sao_Paulo` |
| `clinic.opening-hours.days` | segunda a sexta (`MONDAY` a `FRIDAY`) |
| `clinic.opening-hours.opens-at` | `08:00` |
| `clinic.opening-hours.closes-at` | `18:00` |

Datas e horas são locais, no fuso da clínica, no formato ISO sem fuso (`2030-03-04T10:00:00`). "No passado" quer dizer antes do momento atual nesse fuso. O código inicial já tem um bean `java.time.Clock` nesse fuso.

## Endpoint

`POST /appointments`

```json
{"patientId": 1, "professionalId": 1, "start": "2030-03-04T10:00:00", "end": "2030-03-04T10:30:00"}
```

| Situação | Status |
| --- | --- |
| consulta aceita | `201 Created`, com o corpo abaixo |
| começa no passado | `422 Unprocessable Content` |
| fora do horário de funcionamento: começa antes da abertura, termina depois do fechamento ou cai em dia sem atendimento | `422 Unprocessable Content` |
| sobrepõe outra consulta do **mesmo** profissional | `409 Conflict` |

Corpo da resposta `201`:

```json
{"id": 10, "patientId": 1, "professionalId": 1, "start": "2030-03-04T10:00:00", "end": "2030-03-04T10:30:00", "durationMinutes": 30}
```

- Uma consulta das 08:00 às 08:30 e outra das 17:30 às 18:00 estão dentro do horário.
- Consultas encostadas não se sobrepõem: uma que termina às 11:00 e outra que começa às 11:00 podem existir juntas.
- Profissionais diferentes podem ter consultas no mesmo horário.
- A consulta é gravada na tabela `appointments` (`professional_id`, `patient_id`, `starts_at`, `duration_minutes`). Os testes também inserem consultas direto nessa tabela, e elas contam para a sobreposição.
