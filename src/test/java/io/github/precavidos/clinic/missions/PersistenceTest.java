package io.github.precavidos.clinic.missions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.Patients;
import io.github.precavidos.clinic.support.Patients.PatientData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@Tag(Missions.PERSISTENCE)
@DisplayName("Missão persistence: pacientes no banco de dados")
class PersistenceTest extends IntegrationTest {

	@Autowired
	private Environment environment;

	@Test
	@DisplayName("cria a tabela patients com uma migração Flyway, sem deixar o Hibernate criar o esquema")
	void migrationCreatesPatientsTable() {
		assertPatientsTableExists();
		List<String> columns = jdbc.queryForList("""
				SELECT column_name FROM information_schema.columns
				WHERE table_schema = current_schema() AND table_name = 'patients'
				""", String.class);
		assertThat(columns).as("esperava as colunas id, name, email, phone e birth_date na tabela patients")
			.contains("id", "name", "email", "phone", "birth_date");
		Integer migrations = jdbc.queryForObject(
				"SELECT count(*) FROM flyway_schema_history WHERE success AND version IS NOT NULL AND version <> '1'",
				Integer.class);
		assertThat(migrations).as("esperava uma migração Flyway nova (V2 ou maior) em src/main/resources/db/migration")
			.isPositive();
		assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
			.as("esperava spring.jpa.hibernate.ddl-auto igual a validate ou none: quem cria o esquema é o Flyway")
			.isIn("validate", "none");
	}

	@Test
	@DisplayName("grava na tabela patients o paciente cadastrado pela API")
	void apiWritesToDatabase() {
		assertPatientsTableExists();
		PatientData patient = Patients.newPatient();

		long id = Patients.create(api, patient);

		List<Map<String, Object>> rows = jdbc
			.queryForList("SELECT name, email, phone, birth_date::text AS birth_date FROM patients WHERE id = ?", id);
		assertThat(rows).as("esperava encontrar na tabela patients a linha com id " + id).hasSize(1);
		assertThat(rows.getFirst()).as("esperava os dados do paciente nas colunas da tabela patients")
			.containsEntry("name", patient.name())
			.containsEntry("email", patient.email())
			.containsEntry("phone", patient.phone())
			.containsEntry("birth_date", patient.birthDate());
	}

	@Test
	@DisplayName("lê do banco: um paciente inserido direto na tabela aparece na API")
	void apiReadsFromDatabase() {
		assertPatientsTableExists();
		Long id = jdbc.queryForObject("""
				INSERT INTO patients (name, email, phone, birth_date)
				VALUES ('Paciente Do Banco', 'banco@example.com', '+55 11 90000-9999', DATE '1970-12-01')
				RETURNING id
				""", Long.class);

		ApiResponse response = api.get("/patients/{id}", id);

		assertThat(response.status())
			.as("esperava status 200 ao buscar pela API um paciente gravado no banco; " + response.describe())
			.isEqualTo(200);
		assertThat(response.<String>json("$.name")).as("esperava o nome que está no banco")
			.isEqualTo("Paciente Do Banco");
		assertThat(response.<String>json("$.birthDate")).as("esperava a data de nascimento que está no banco")
			.isEqualTo("1970-12-01");
	}

	@Test
	@DisplayName("altera e remove o paciente no banco quando a API altera e remove")
	void updateAndDeleteReachDatabase() {
		assertPatientsTableExists();
		long id = Patients.create(api);
		PatientData changed = new PatientData("Nome Novo No Banco", "novo" + id + "@example.com",
				"+55 31 97777-0000", "2001-07-07");

		api.put("/patients/{id}", changed.toJson(), id);
		String name = jdbc.queryForObject("SELECT name FROM patients WHERE id = ?", String.class, id);
		api.delete("/patients/{id}", id);
		Integer remaining = jdbc.queryForObject("SELECT count(*) FROM patients WHERE id = ?", Integer.class, id);

		assertThat(name).as("esperava o nome alterado pela API na tabela patients").isEqualTo(changed.name());
		assertThat(remaining).as("esperava que o paciente removido pela API saísse da tabela patients").isZero();
	}

	private void assertPatientsTableExists() {
		assertThat(fixtures.tableExists("patients"))
			.as("esperava a tabela patients no PostgreSQL, criada por uma migração Flyway")
			.isTrue();
	}

}
