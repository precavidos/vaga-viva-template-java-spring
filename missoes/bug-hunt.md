# Missão `bug-hunt`: caça ao bug

Encontre e corrija um defeito escondido no cálculo da duração das consultas.

Testes: `src/test/java/io/github/precavidos/clinic/missions/BugHuntTest.java`. Para rodar só esta missão: `./mvnw test -Dgroups=bug-hunt`.

## Contrato

- O cálculo fica em `io.github.precavidos.clinic.appointment.AppointmentDuration.between(start, end)`, que devolve a duração em minutos entre dois `LocalDateTime`.
- O resultado precisa estar certo para qualquer par de datas em que `end` vem depois de `start`, inclusive em dias diferentes.
- Mantenha a assinatura do método: outras partes do sistema o usam.
- Os testes da missão mostram os casos em que o resultado sai errado. Antes de corrigir, entenda por quê.
