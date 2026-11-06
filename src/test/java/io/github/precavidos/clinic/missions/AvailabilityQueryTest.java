package io.github.precavidos.clinic.missions;

import static io.github.precavidos.clinic.support.ClinicDates.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.ClinicDates;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.AVAILABILITY_QUERY)
@DisplayName("Missão availability-query: consulta de horários livres")
class AvailabilityQueryTest extends IntegrationTest {

	private static final long PATIENT_ID = 1;

	private final LocalDate monday = ClinicDates.future(DayOfWeek.MONDAY);

	private long professionalId;

	@BeforeEach
	void setUp() {
		professionalId = fixtures.professional();
	}

	@Test
	@DisplayName("um dia útil sem consultas fica livre das 08:00 às 18:00")
	void emptyDayIsFree() {
		List<Slot> free = availability(professionalId, monday);

		assertThat(free).as("esperava um único intervalo livre das 08:00 às 18:00")
			.containsExactly(slot(monday, "08:00", "18:00"));
	}

	@Test
	@DisplayName("devolve os intervalos entre as consultas, em ordem")
	void gapsBetweenAppointments() {
		fixtures.appointment(professionalId, PATIENT_ID, at(monday, "13:00"), 90);
		fixtures.appointment(professionalId, PATIENT_ID, at(monday, "09:00"), 60);

		List<Slot> free = availability(professionalId, monday);

		assertThat(free).as("esperava os intervalos livres 08:00-09:00, 10:00-13:00 e 14:30-18:00")
			.containsExactly(slot(monday, "08:00", "09:00"), slot(monday, "10:00", "13:00"),
					slot(monday, "14:30", "18:00"));
	}

	@Test
	@DisplayName("ignora consultas de outros profissionais e de outros dias")
	void ignoresOtherProfessionalsAndDays() {
		long otherProfessionalId = fixtures.professional("Dr. Rafael Moura", "Cardiologia");
		fixtures.appointment(otherProfessionalId, PATIENT_ID, at(monday, "10:00"), 60);
		fixtures.appointment(professionalId, PATIENT_ID, at(monday.plusDays(1), "10:00"), 60);
		fixtures.appointment(professionalId, PATIENT_ID, at(monday, "15:00"), 30);

		List<Slot> free = availability(professionalId, monday);

		assertThat(free).as("esperava só a consulta das 15:00 do próprio profissional, nesse dia, ocupando a agenda")
			.containsExactly(slot(monday, "08:00", "15:00"), slot(monday, "15:30", "18:00"));
	}

	@Test
	@DisplayName("não devolve intervalos vazios entre consultas encostadas nem nas pontas do dia")
	void noEmptyGaps() {
		fixtures.appointment(professionalId, PATIENT_ID, at(monday, "08:00"), 60);
		fixtures.appointment(professionalId, PATIENT_ID, at(monday, "09:00"), 60);
		fixtures.appointment(professionalId, PATIENT_ID, at(monday, "17:00"), 60);

		List<Slot> free = availability(professionalId, monday);

		assertThat(free).as("esperava só o intervalo livre das 10:00 às 17:00")
			.containsExactly(slot(monday, "10:00", "17:00"));
	}

	@Test
	@DisplayName("não há horários livres no fim de semana")
	void weekendHasNoAvailability() {
		LocalDate saturday = ClinicDates.future(DayOfWeek.SATURDAY);
		LocalDate sunday = ClinicDates.future(DayOfWeek.SUNDAY);

		assertThat(availability(professionalId, saturday)).as("esperava lista vazia no sábado").isEmpty();
		assertThat(availability(professionalId, sunday)).as("esperava lista vazia no domingo").isEmpty();
	}

	private List<Slot> availability(long professionalId, LocalDate date) {
		ApiResponse response = api.get("/professionals/{id}/availability?date={date}", professionalId, date);
		assertThat(response.status())
			.as("esperava status 200 em GET /professionals/{id}/availability?date=" + date + "; " + response.describe())
			.isEqualTo(200);
		List<Map<String, Object>> slots = response.json("$");
		assertThat(slots).as("esperava uma lista JSON de intervalos {start, end}; " + response.describe()).isNotNull();
		return slots.stream()
			.map((slot) -> new Slot(parse(slot.get("start")), parse(slot.get("end"))))
			.toList();
	}

	private static LocalDateTime parse(Object value) {
		return value == null ? null : LocalDateTime.parse(value.toString());
	}

	private static Slot slot(LocalDate date, String start, String end) {
		return new Slot(at(date, start), at(date, end));
	}

	record Slot(LocalDateTime start, LocalDateTime end) {
	}

}
