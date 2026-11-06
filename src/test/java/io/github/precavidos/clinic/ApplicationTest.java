package io.github.precavidos.clinic;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Aplicação")
class ApplicationTest extends IntegrationTest {

	@Test
	@DisplayName("sobe e responde em /actuator/health sem pedir credenciais")
	void healthIsUp() {
		ApiResponse response = api.anonymous().get("/actuator/health");

		assertThat(response.status()).as("esperava status 200 em /actuator/health; " + response.describe())
			.isEqualTo(200);
		assertThat(response.<String>json("$.status")).as("esperava status UP; " + response.describe()).isEqualTo("UP");
	}

	@Test
	@DisplayName("cria as tabelas de profissionais e consultas com a migração inicial")
	void initialMigrationCreatesTables() {
		assertThat(fixtures.tableExists("professionals")).as("esperava a tabela professionals").isTrue();
		assertThat(fixtures.tableExists("appointments")).as("esperava a tabela appointments").isTrue();
	}

}
