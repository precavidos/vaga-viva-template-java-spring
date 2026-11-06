package io.github.precavidos.clinic.missions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import io.github.precavidos.clinic.appointment.AppointmentDuration;
import io.github.precavidos.clinic.support.Missions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.BUG_HUNT)
@DisplayName("Missão bug-hunt: caça ao bug na duração das consultas")
class BugHuntTest {

	@Test
	@DisplayName("calcula a duração de uma consulta que termina no dia seguinte")
	void endsOnTheNextDay() {
		int minutes = AppointmentDuration.between(LocalDateTime.parse("2030-03-04T23:30"),
				LocalDateTime.parse("2030-03-05T00:30"));

		assertThat(minutes).as("esperava 60 minutos entre 23:30 e 00:30 do dia seguinte").isEqualTo(60);
	}

	@Test
	@DisplayName("calcula a duração de um plantão noturno de várias horas")
	void overnightShift() {
		int minutes = AppointmentDuration.between(LocalDateTime.parse("2030-03-04T22:00"),
				LocalDateTime.parse("2030-03-05T02:15"));

		assertThat(minutes).as("esperava 255 minutos entre 22:00 e 02:15 do dia seguinte").isEqualTo(255);
	}

	@Test
	@DisplayName("calcula a duração de um período de exatamente 24 horas")
	void exactlyOneDay() {
		int minutes = AppointmentDuration.between(LocalDateTime.parse("2030-03-04T10:00"),
				LocalDateTime.parse("2030-03-05T10:00"));

		assertThat(minutes).as("esperava 1440 minutos em 24 horas").isEqualTo(1440);
	}

	@Test
	@DisplayName("calcula a duração na virada do ano")
	void newYearsEve() {
		int minutes = AppointmentDuration.between(LocalDateTime.parse("2030-12-31T23:45"),
				LocalDateTime.parse("2031-01-01T00:15"));

		assertThat(minutes).as("esperava 30 minutos entre 23:45 de 31/12 e 00:15 de 01/01").isEqualTo(30);
	}

}
