package io.github.precavidos.clinic.missions;

import static io.github.precavidos.clinic.support.ClinicDates.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.ClinicDates;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.Patients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.SCHEDULING_RULES)
@DisplayName("Missão scheduling-rules: regras de agendamento")
class SchedulingRulesTest extends IntegrationTest {

	private final LocalDate monday = ClinicDates.future(DayOfWeek.MONDAY);

	private long patientId;

	private long professionalId;

	@BeforeEach
	void setUp() {
		patientId = Patients.create(api);
		professionalId = fixtures.professional();
	}

	@Test
	@DisplayName("agenda uma consulta dentro do horário de funcionamento")
	void schedulesAppointment() {
		LocalDateTime start = at(monday, "10:00");
		LocalDateTime end = at(monday, "10:30");

		ApiResponse response = schedule(professionalId, start, end);

		assertThat(response.status()).as("esperava status 201 em POST /appointments; " + response.describe())
			.isEqualTo(201);
		assertThat(response.<Number>json("$.id")).as("esperava o campo 'id' na resposta").isNotNull();
		assertThat(response.<Number>json("$.professionalId")).as("esperava o campo 'professionalId' na resposta")
			.extracting(Number::longValue)
			.isEqualTo(professionalId);
		assertThat(response.<Number>json("$.patientId")).as("esperava o campo 'patientId' na resposta")
			.extracting(Number::longValue)
			.isEqualTo(patientId);
		assertThat(dateTime(response, "$.start")).as("esperava o campo 'start' na resposta").isEqualTo(start);
		assertThat(dateTime(response, "$.end")).as("esperava o campo 'end' na resposta").isEqualTo(end);
		Map<String, Object> row = jdbc.queryForMap(
				"SELECT professional_id, patient_id, starts_at, duration_minutes FROM appointments WHERE id = ?",
				response.<Number>json("$.id").longValue());
		assertThat(row).as("esperava a consulta gravada na tabela appointments com duração de 30 minutos")
			.containsEntry("professional_id", professionalId)
			.containsEntry("patient_id", patientId)
			.containsEntry("duration_minutes", 30);
	}

	@Test
	@DisplayName("recusa com 422 uma consulta no passado")
	void rejectsPastAppointment() {
		LocalDate pastMonday = ClinicDates.past(DayOfWeek.MONDAY);

		ApiResponse response = schedule(professionalId, at(pastMonday, "10:00"), at(pastMonday, "10:30"));

		assertThat(response.status())
			.as("esperava recusar consulta no passado com status 422; " + response.describe())
			.isEqualTo(422);
		assertThat(fixtures.countAppointments()).as("esperava que a consulta recusada não fosse gravada").isZero();
	}

	@Test
	@DisplayName("recusa com 422 consultas fora do horário de funcionamento")
	void rejectsOutsideOpeningHours() {
		LocalDate saturday = ClinicDates.future(DayOfWeek.SATURDAY);

		ApiResponse beforeOpening = schedule(professionalId, at(monday, "07:30"), at(monday, "08:30"));
		ApiResponse afterClosing = schedule(professionalId, at(monday, "17:30"), at(monday, "18:30"));
		ApiResponse onSaturday = schedule(professionalId, at(saturday, "10:00"), at(saturday, "10:30"));

		assertThat(beforeOpening.status())
			.as("esperava recusar com 422 consulta que começa antes das 08:00; " + beforeOpening.describe())
			.isEqualTo(422);
		assertThat(afterClosing.status())
			.as("esperava recusar com 422 consulta que termina depois das 18:00; " + afterClosing.describe())
			.isEqualTo(422);
		assertThat(onSaturday.status()).as("esperava recusar com 422 consulta no sábado; " + onSaturday.describe())
			.isEqualTo(422);
		assertThat(fixtures.countAppointments()).as("esperava que as consultas recusadas não fossem gravadas").isZero();
	}

	@Test
	@DisplayName("aceita consultas que começam na abertura e terminam no fechamento")
	void acceptsOpeningHoursBoundaries() {
		ApiResponse atOpening = schedule(professionalId, at(monday, "08:00"), at(monday, "08:30"));
		ApiResponse atClosing = schedule(professionalId, at(monday, "17:30"), at(monday, "18:00"));

		assertThat(atOpening.status()).as("esperava aceitar consulta das 08:00 às 08:30; " + atOpening.describe())
			.isEqualTo(201);
		assertThat(atClosing.status()).as("esperava aceitar consulta das 17:30 às 18:00; " + atClosing.describe())
			.isEqualTo(201);
	}

	@Test
	@DisplayName("recusa com 409 uma consulta que se sobrepõe a outra do mesmo profissional")
	void rejectsOverlap() {
		fixtures.appointment(professionalId, patientId, at(monday, "10:00"), 60);

		ApiResponse overlapsEnd = schedule(professionalId, at(monday, "10:30"), at(monday, "11:30"));
		ApiResponse overlapsStart = schedule(professionalId, at(monday, "09:30"), at(monday, "10:30"));
		ApiResponse contains = schedule(professionalId, at(monday, "09:00"), at(monday, "12:00"));

		assertThat(overlapsEnd.status())
			.as("esperava recusar com 409 consulta das 10:30 às 11:30 sobre outra das 10:00 às 11:00; "
					+ overlapsEnd.describe())
			.isEqualTo(409);
		assertThat(overlapsStart.status())
			.as("esperava recusar com 409 consulta das 09:30 às 10:30 sobre outra das 10:00 às 11:00; "
					+ overlapsStart.describe())
			.isEqualTo(409);
		assertThat(contains.status())
			.as("esperava recusar com 409 consulta das 09:00 às 12:00 sobre outra das 10:00 às 11:00; "
					+ contains.describe())
			.isEqualTo(409);
		assertThat(fixtures.countAppointments()).as("esperava que só a consulta original estivesse gravada")
			.isEqualTo(1);
	}

	@Test
	@DisplayName("aceita consultas encostadas e consultas no mesmo horário com outro profissional")
	void acceptsAdjacentAndOtherProfessional() {
		long otherProfessionalId = fixtures.professional("Dr. Rafael Moura", "Cardiologia");
		fixtures.appointment(professionalId, patientId, at(monday, "10:00"), 60);

		ApiResponse after = schedule(professionalId, at(monday, "11:00"), at(monday, "11:30"));
		ApiResponse before = schedule(professionalId, at(monday, "09:00"), at(monday, "10:00"));
		ApiResponse other = schedule(otherProfessionalId, at(monday, "10:00"), at(monday, "11:00"));

		assertThat(after.status())
			.as("esperava aceitar consulta que começa às 11:00, quando a anterior termina; " + after.describe())
			.isEqualTo(201);
		assertThat(before.status())
			.as("esperava aceitar consulta que termina às 10:00, quando a próxima começa; " + before.describe())
			.isEqualTo(201);
		assertThat(other.status())
			.as("esperava aceitar consulta no mesmo horário com outro profissional; " + other.describe())
			.isEqualTo(201);
	}

	private ApiResponse schedule(long professionalId, LocalDateTime start, LocalDateTime end) {
		return api.post("/appointments", """
				{"patientId": %d, "professionalId": %d, "start": "%s", "end": "%s"}
				""".formatted(patientId, professionalId, start, end));
	}

	private static LocalDateTime dateTime(ApiResponse response, String path) {
		String value = response.json(path);
		return value == null ? null : LocalDateTime.parse(value);
	}

}
