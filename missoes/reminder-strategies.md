# Missão `reminder-strategies`: lembretes por e-mail e SMS

Envie lembretes de consulta por canais diferentes, aplicando um padrão de projeto.

Testes: `src/test/java/io/github/precavidos/clinic/missions/ReminderStrategiesTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=reminder-strategies`.

## O que o código inicial já traz

Pacote `io.github.precavidos.clinic.reminder`:

- `EmailSender` e `SmsSender`: quem de fato envia. As implementações do código inicial só escrevem no log; nos testes, elas são trocadas por versões que guardam o que foi enviado.
- `ReminderChannel`: os canais, `EMAIL` e `SMS`.
- `ReminderStrategy`: o contrato de um canal de lembrete.
- `Reminder`: os dados de um lembrete.

## Contrato

- Cada canal tem a sua implementação de `ReminderStrategy`, registrada como bean do Spring, uma classe por canal.
- `POST /appointments/{id}/reminders?channel=EMAIL` ou `?channel=SMS` envia o lembrete da consulta `{id}` pelo canal pedido e responde `202 Accepted`.
- E-mail: enviado por `EmailSender` para o e-mail do paciente, com assunto.
- SMS: enviado por `SmsSender` para o telefone do paciente.
- O texto traz o nome do profissional, a data no formato `dd/MM/aaaa` e a hora no formato `HH:mm`. Exemplo: `Lembrete: consulta com Dr. Rafael Moura em 04/03/2030 às 10:15.`
- Um lembrete por um canal não envia nada pelo outro.
