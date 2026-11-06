package io.github.precavidos.clinic.missions;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.Patients;
import io.github.precavidos.clinic.support.Patients.PatientData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(Missions.PATIENTS_API)
@DisplayName("Missão patients-api: cadastro de pacientes")
class PatientsApiTest extends IntegrationTest {

	@Test
	@DisplayName("cadastra um paciente e devolve 201, o cabeçalho Location e os dados com o id")
	void createsPatient() {
		PatientData patient = Patients.newPatient();

		ApiResponse response = api.post("/patients", patient.toJson());

		assertThat(response.status()).as("esperava status 201 em POST /patients; " + response.describe())
			.isEqualTo(201);
		Number id = response.json("$.id");
		assertThat(id).as("esperava o campo 'id' na resposta; " + response.describe()).isNotNull();
		assertThat(response.header("Location")).as("esperava o cabeçalho Location apontando para /patients/{id}")
			.isNotNull()
			.endsWith("/patients/" + id);
		assertThat(response.<String>json("$.name")).as("esperava o campo 'name' na resposta").isEqualTo(patient.name());
		assertThat(response.<String>json("$.email")).as("esperava o campo 'email' na resposta")
			.isEqualTo(patient.email());
		assertThat(response.<String>json("$.phone")).as("esperava o campo 'phone' na resposta")
			.isEqualTo(patient.phone());
		assertThat(response.<String>json("$.birthDate")).as("esperava o campo 'birthDate' (AAAA-MM-DD) na resposta")
			.isEqualTo(patient.birthDate());
	}

	@Test
	@DisplayName("busca um paciente cadastrado pelo id")
	void findsPatientById() {
		PatientData patient = Patients.newPatient();
		long id = Patients.create(api, patient);

		ApiResponse response = api.get("/patients/{id}", id);

		assertThat(response.status()).as("esperava status 200 em GET /patients/" + id + "; " + response.describe())
			.isEqualTo(200);
		assertThat(response.<Number>json("$.id")).as("esperava o mesmo id na resposta")
			.extracting(Number::longValue)
			.isEqualTo(id);
		assertThat(response.<String>json("$.name")).as("esperava o nome cadastrado").isEqualTo(patient.name());
		assertThat(response.<String>json("$.email")).as("esperava o e-mail cadastrado").isEqualTo(patient.email());
	}

	@Test
	@DisplayName("altera os dados de um paciente")
	void updatesPatient() {
		long id = Patients.create(api);
		PatientData changed = new PatientData("Paciente Alterado", "alterado" + id + "@example.com",
				"+55 21 98888-0000", "1985-01-30");

		ApiResponse update = api.put("/patients/{id}", changed.toJson(), id);

		assertThat(update.status()).as("esperava status 200 em PUT /patients/" + id + "; " + update.describe())
			.isEqualTo(200);
		assertThat(update.<String>json("$.name")).as("esperava o nome novo na resposta do PUT")
			.isEqualTo(changed.name());
		ApiResponse found = api.get("/patients/{id}", id);
		assertThat(found.<String>json("$.name")).as("esperava o nome novo ao buscar o paciente; " + found.describe())
			.isEqualTo(changed.name());
		assertThat(found.<String>json("$.email")).as("esperava o e-mail novo ao buscar o paciente")
			.isEqualTo(changed.email());
		assertThat(found.<String>json("$.phone")).as("esperava o telefone novo ao buscar o paciente")
			.isEqualTo(changed.phone());
		assertThat(found.<String>json("$.birthDate")).as("esperava a data de nascimento nova ao buscar o paciente")
			.isEqualTo(changed.birthDate());
	}

	@Test
	@DisplayName("remove um paciente, que deixa de ser encontrado")
	void deletesPatient() {
		long id = Patients.create(api);

		ApiResponse delete = api.delete("/patients/{id}", id);

		assertThat(delete.status()).as("esperava status 204 em DELETE /patients/" + id + "; " + delete.describe())
			.isEqualTo(204);
		ApiResponse found = api.get("/patients/{id}", id);
		assertThat(found.status()).as("esperava status 404 ao buscar um paciente removido; " + found.describe())
			.isEqualTo(404);
	}

	@Test
	@DisplayName("responde 404 ao alterar ou remover um paciente que não existe")
	void unknownPatientIsNotFound() {
		long id = Patients.create(api);
		api.delete("/patients/{id}", id);
		PatientData patient = Patients.newPatient();

		ApiResponse update = api.put("/patients/{id}", patient.toJson(), id);
		ApiResponse delete = api.delete("/patients/{id}", id);

		assertThat(update.status()).as("esperava status 404 ao alterar um paciente removido; " + update.describe())
			.isEqualTo(404);
		assertThat(delete.status()).as("esperava status 404 ao remover de novo um paciente; " + delete.describe())
			.isEqualTo(404);
	}

}
