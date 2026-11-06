package io.github.precavidos.clinic.missions;

import static io.github.precavidos.clinic.support.ClinicDates.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.ClinicDates;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.PAGINATION)
@DisplayName("Missão pagination: listagem paginada de consultas")
class PaginationTest extends IntegrationTest {

	private static final long PATIENT_ID = 1;

	private final LocalDateTime mon09 = at(ClinicDates.future(DayOfWeek.MONDAY), "09:00");

	private final LocalDateTime mon14 = at(ClinicDates.future(DayOfWeek.MONDAY), "14:00");

	private final LocalDateTime tue10 = at(ClinicDates.future(DayOfWeek.TUESDAY), "10:00");

	private final LocalDateTime wed08 = at(ClinicDates.future(DayOfWeek.WEDNESDAY), "08:00");

	private final LocalDateTime thu16 = at(ClinicDates.future(DayOfWeek.THURSDAY), "16:00");

	private long professionalId;

	private long otherProfessionalId;

	@BeforeEach
	void setUp() {
		professionalId = fixtures.professional();
		otherProfessionalId = fixtures.professional("Dr. Rafael Moura", "Cardiologia");
		fixtures.appointment(professionalId, PATIENT_ID, tue10, 30);
		fixtures.appointment(professionalId, PATIENT_ID, thu16, 60);
		fixtures.appointment(professionalId, PATIENT_ID, mon14, 30);
		fixtures.appointment(professionalId, PATIENT_ID, wed08, 45);
		fixtures.appointment(professionalId, PATIENT_ID, mon09, 30);
		fixtures.appointment(otherProfessionalId, PATIENT_ID, mon09, 30);
		fixtures.appointment(otherProfessionalId, PATIENT_ID, tue10, 30);
	}

	@Test
	@DisplayName("devolve a primeira página ordenada pela data de início")
	void firstPageSortedByStart() {
		ApiResponse response = list("?page=0&size=2", professionalId);

		assertOk(response);
		assertThat(starts(response)).as("esperava as 2 primeiras consultas, em ordem crescente de início")
			.containsExactly(mon09, mon14);
		assertPage(response, 0, 2, 5, 3);
	}

	@Test
	@DisplayName("devolve a última página com os dados de cada consulta")
	void lastPageWithAppointmentFields() {
		ApiResponse response = list("?page=2&size=2", professionalId);

		assertOk(response);
		assertThat(starts(response)).as("esperava só a última consulta na página 2").containsExactly(thu16);
		assertThat(response.<Number>json("$.content[0].id")).as("esperava o campo 'id' em cada consulta").isNotNull();
		assertThat(response.<Number>json("$.content[0].professionalId"))
			.as("esperava o campo 'professionalId' em cada consulta")
			.extracting(Number::longValue)
			.isEqualTo(professionalId);
		assertThat(response.<Number>json("$.content[0].patientId")).as("esperava o campo 'patientId' em cada consulta")
			.extracting(Number::longValue)
			.isEqualTo(PATIENT_ID);
		String end = response.json("$.content[0].end");
		assertThat(end).as("esperava o campo 'end' em cada consulta").isNotNull();
		assertThat(LocalDateTime.parse(end)).as("esperava 'end' = início + duração").isEqualTo(thu16.plusMinutes(60));
		assertThat(response.<Number>json("$.content[0].durationMinutes"))
			.as("esperava o campo 'durationMinutes' em cada consulta")
			.extracting(Number::intValue)
			.isEqualTo(60);
		assertPage(response, 2, 2, 5, 3);
	}

	@Test
	@DisplayName("lista só as consultas do profissional, com 20 itens por página por padrão")
	void onlyTheProfessionalsAppointmentsWithDefaultSize() {
		ApiResponse response = list("", professionalId);

		assertOk(response);
		assertThat(starts(response)).as("esperava as 5 consultas do profissional, sem as de outro profissional")
			.containsExactly(mon09, mon14, tue10, wed08, thu16);
		List<Number> professionals = response.json("$.content[*].professionalId");
		assertThat(professionals).as("esperava só consultas do profissional pedido")
			.allSatisfy((id) -> assertThat(id.longValue()).isEqualTo(professionalId));
		assertPage(response, 0, 20, 5, 1);
	}

	@Test
	@DisplayName("ordena da mais recente para a mais antiga com sort=start,desc")
	void sortsDescending() {
		ApiResponse response = list("?page=0&size=3&sort=start,desc", professionalId);

		assertOk(response);
		assertThat(starts(response)).as("esperava as consultas em ordem decrescente de início")
			.containsExactly(thu16, wed08, tue10);
		assertPage(response, 0, 3, 5, 2);
	}

	@Test
	@DisplayName("devolve uma página vazia para um profissional sem consultas")
	void emptyPage() {
		long withoutAppointments = fixtures.professional("Dra. Beatriz Lins", "Pediatria");

		ApiResponse response = list("?page=0&size=10", withoutAppointments);

		assertOk(response);
		assertThat(response.<List<Object>>json("$.content")).as("esperava a lista 'content' vazia").isEmpty();
		assertPage(response, 0, 10, 0, 0);
	}

	private ApiResponse list(String query, long professionalId) {
		return api.get("/professionals/{id}/appointments" + query, professionalId);
	}

	private static void assertOk(ApiResponse response) {
		assertThat(response.status())
			.as("esperava status 200 em GET /professionals/{id}/appointments; " + response.describe())
			.isEqualTo(200);
	}

	private static List<LocalDateTime> starts(ApiResponse response) {
		List<String> starts = response.json("$.content[*].start");
		assertThat(starts).as("esperava a lista 'content' com o campo 'start' de cada consulta; " + response.describe())
			.isNotNull();
		return starts.stream().map(LocalDateTime::parse).toList();
	}

	private static void assertPage(ApiResponse response, int number, int size, int totalElements, int totalPages) {
		assertThat(response.<Number>json("$.page.number")).as("esperava page.number = " + number)
			.extracting(Number::intValue)
			.isEqualTo(number);
		assertThat(response.<Number>json("$.page.size")).as("esperava page.size = " + size)
			.extracting(Number::intValue)
			.isEqualTo(size);
		assertThat(response.<Number>json("$.page.totalElements")).as("esperava page.totalElements = " + totalElements)
			.extracting(Number::intValue)
			.isEqualTo(totalElements);
		assertThat(response.<Number>json("$.page.totalPages")).as("esperava page.totalPages = " + totalPages)
			.extracting(Number::intValue)
			.isEqualTo(totalPages);
	}

}
