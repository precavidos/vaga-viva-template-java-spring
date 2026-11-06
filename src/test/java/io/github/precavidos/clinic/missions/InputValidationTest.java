package io.github.precavidos.clinic.missions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.Patients;
import io.github.precavidos.clinic.support.Patients.PatientData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.INPUT_VALIDATION)
@DisplayName("Missão input-validation: validação e mensagens de erro")
class InputValidationTest extends IntegrationTest {

	private static final Set<String> ENGLISH_DEFAULT_MESSAGES = Set.of("must not be blank", "must not be null",
			"must not be empty", "must be a well-formed email address", "must be a past date",
			"must be a date in the past or in the present", "invalid", "Invalid request content.",
			"Failed to read request");

	@Test
	@DisplayName("recusa paciente sem nome com 400 e Problem Details apontando o campo")
	void rejectsBlankName() {
		PatientData patient = Patients.newPatient();

		ApiResponse response = api.post("/patients", withName(patient, " ").toJson());

		assertValidationProblem(response, "name");
	}

	@Test
	@DisplayName("recusa e-mail inválido")
	void rejectsInvalidEmail() {
		PatientData patient = Patients.newPatient();
		PatientData invalid = new PatientData(patient.name(), "nao-e-um-email", patient.phone(), patient.birthDate());

		ApiResponse response = api.post("/patients", invalid.toJson());

		assertValidationProblem(response, "email");
	}

	@Test
	@DisplayName("recusa data de nascimento no futuro")
	void rejectsFutureBirthDate() {
		PatientData patient = Patients.newPatient();
		PatientData invalid = new PatientData(patient.name(), patient.email(), patient.phone(),
				LocalDate.now().plusYears(1).toString());

		ApiResponse response = api.post("/patients", invalid.toJson());

		assertValidationProblem(response, "birthDate");
	}

	@Test
	@DisplayName("aponta todos os campos obrigatórios que faltam de uma vez")
	void reportsAllMissingFields() {
		ApiResponse response = api.post("/patients", "{}");

		assertValidationProblem(response, "name", "email", "phone", "birthDate");
	}

	@Test
	@DisplayName("valida também a alteração de um paciente")
	void validatesUpdate() {
		long id = Patients.create(api);
		PatientData invalid = withName(Patients.newPatient(), "");

		ApiResponse response = api.put("/patients/{id}", invalid.toJson(), id);

		assertValidationProblem(response, "name");
	}

	@Test
	@DisplayName("recusa JSON malformado com 400 e Problem Details")
	void rejectsMalformedJson() {
		ApiResponse response = api.post("/patients", "{\"name\": ");

		assertProblem(response);
	}

	private static PatientData withName(PatientData patient, String name) {
		return new PatientData(name, patient.email(), patient.phone(), patient.birthDate());
	}

	private static void assertProblem(ApiResponse response) {
		assertThat(response.status()).as("esperava status 400 para dados inválidos; " + response.describe())
			.isEqualTo(400);
		assertThat(response.contentType())
			.as("esperava Content-Type application/problem+json (Problem Details, RFC 9457); " + response.describe())
			.startsWith("application/problem+json");
		assertThat(response.<Number>json("$.status")).as("esperava o campo 'status' igual a 400 no corpo do erro")
			.extracting(Number::intValue)
			.isEqualTo(400);
		assertThat(response.<String>json("$.title")).as("esperava o campo 'title' no corpo do erro").isNotBlank();
		String detail = response.json("$.detail");
		assertThat(detail).as("esperava o campo 'detail', em português, no corpo do erro; " + response.describe())
			.isNotBlank()
			.isNotIn(ENGLISH_DEFAULT_MESSAGES);
	}

	private static void assertValidationProblem(ApiResponse response, String... fields) {
		assertProblem(response);
		List<Map<String, Object>> errors = response.json("$.errors");
		assertThat(errors).as("esperava a lista 'errors' com um item {field, message} por campo inválido; "
				+ response.describe())
			.isNotNull()
			.isNotEmpty();
		List<Object> reportedFields = errors.stream().map((error) -> error.get("field")).toList();
		assertThat(reportedFields).as("esperava erros para os campos " + List.of(fields) + "; " + response.describe())
			.contains((Object[]) fields);
		for (Map<String, Object> error : errors) {
			assertThat(error.get("message"))
				.as("esperava uma mensagem clara, em português, para o campo " + error.get("field"))
				.isInstanceOf(String.class)
				.asString()
				.isNotBlank()
				.isNotIn(ENGLISH_DEFAULT_MESSAGES);
		}
	}

}
