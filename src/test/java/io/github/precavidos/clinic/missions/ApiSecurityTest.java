package io.github.precavidos.clinic.missions;

import static io.github.precavidos.clinic.support.ClinicDates.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;

import io.github.precavidos.clinic.support.Api;
import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.ClinicDates;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.Patients;
import io.github.precavidos.clinic.support.TestUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.API_SECURITY)
@DisplayName("Missão api-security: proteção da API")
class ApiSecurityTest extends IntegrationTest {

	@Test
	@DisplayName("sem credenciais, recusa a API com 401 e mantém /actuator/health público")
	void requiresAuthentication() {
		Api anonymous = api.anonymous();

		ApiResponse get = anonymous.get("/patients/{id}", 1);
		ApiResponse post = anonymous.post("/patients", Patients.newPatient().toJson());
		ApiResponse health = anonymous.get("/actuator/health");

		assertThat(get.status()).as("esperava 401 em GET /patients/1 sem credenciais; " + get.describe())
			.isEqualTo(401);
		assertThat(post.status()).as("esperava 401 em POST /patients sem credenciais; " + post.describe())
			.isEqualTo(401);
		assertThat(health.status()).as("esperava /actuator/health público (200) sem credenciais; " + health.describe())
			.isEqualTo(200);
	}

	@Test
	@DisplayName("recusa com 401 senha errada e usuário desconhecido")
	void rejectsWrongCredentials() {
		ApiResponse wrongPassword = api.withCredentials(TestUser.RECEPTIONIST.username(), "senha-errada")
			.get("/patients/{id}", 1);
		ApiResponse unknownUser = api.withCredentials("ninguem", "qualquer").get("/patients/{id}", 1);

		assertThat(wrongPassword.status()).as("esperava 401 com senha errada; " + wrongPassword.describe())
			.isEqualTo(401);
		assertThat(unknownUser.status()).as("esperava 401 com usuário desconhecido; " + unknownUser.describe())
			.isEqualTo(401);
	}

	@Test
	@DisplayName("o profissional consulta pacientes, mas não cadastra, altera nem remove")
	void professionalCanOnlyReadPatients() {
		long id = Patients.create(api);
		Api professional = api.as(TestUser.PROFESSIONAL);

		ApiResponse get = professional.get("/patients/{id}", id);
		ApiResponse post = professional.post("/patients", Patients.newPatient().toJson());
		ApiResponse put = professional.put("/patients/{id}", Patients.newPatient().toJson(), id);
		ApiResponse delete = professional.delete("/patients/{id}", id);

		assertThat(get.status()).as("esperava 200 quando o profissional consulta um paciente; " + get.describe())
			.isEqualTo(200);
		assertThat(post.status()).as("esperava 403 quando o profissional tenta cadastrar paciente; " + post.describe())
			.isEqualTo(403);
		assertThat(put.status()).as("esperava 403 quando o profissional tenta alterar paciente; " + put.describe())
			.isEqualTo(403);
		assertThat(delete.status()).as("esperava 403 quando o profissional tenta remover paciente; " + delete.describe())
			.isEqualTo(403);
		assertThat(get.header("Set-Cookie")).as("esperava uma API sem sessão: nenhum cookie na resposta").isNull();
	}

	@Test
	@DisplayName("a recepcionista agenda consultas, o profissional não")
	void onlyReceptionistSchedules() {
		long patientId = Patients.create(api);
		long professionalId = fixtures.professional();
		LocalDate monday = ClinicDates.future(DayOfWeek.MONDAY);
		String byProfessional = appointment(patientId, professionalId, monday, "10:00", "10:30");
		String byReceptionist = appointment(patientId, professionalId, monday, "11:00", "11:30");

		ApiResponse denied = api.as(TestUser.PROFESSIONAL).post("/appointments", byProfessional);
		ApiResponse allowed = api.as(TestUser.RECEPTIONIST).post("/appointments", byReceptionist);

		assertThat(denied.status()).as("esperava 403 quando o profissional tenta agendar; " + denied.describe())
			.isEqualTo(403);
		assertThat(allowed.status()).as("esperava 201 quando a recepcionista agenda; " + allowed.describe())
			.isEqualTo(201);
	}

	private static String appointment(long patientId, long professionalId, LocalDate date, String start, String end) {
		return """
				{"patientId": %d, "professionalId": %d, "start": "%s", "end": "%s"}
				""".formatted(patientId, professionalId, at(date, start), at(date, end));
	}

}
