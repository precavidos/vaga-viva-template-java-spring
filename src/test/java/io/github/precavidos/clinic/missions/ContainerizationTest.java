package io.github.precavidos.clinic.missions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.yaml.snakeyaml.Yaml;

@Tag(Missions.CONTAINERIZATION)
@DisplayName("Missão containerization: aplicação em contêiner")
class ContainerizationTest {

	private static final Path DOCKERFILE = Path.of("Dockerfile");

	private static final Path COMPOSE = Path.of("compose.yaml");

	@Test
	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	@DisplayName("a imagem do Dockerfile sobe e responde em /actuator/health conectada ao PostgreSQL")
	void imageStartsAndIsHealthy() throws Exception {
		assertThat(Files.isRegularFile(DOCKERFILE)).as("esperava encontrar o arquivo Dockerfile na raiz do projeto")
			.isTrue();

		try (Network network = Network.newNetwork();
				PostgreSQLContainer database = new PostgreSQLContainer(TestcontainersConfiguration.POSTGRES_IMAGE)
					.withNetwork(network)
					.withNetworkAliases("db")
					.withDatabaseName("clinic")
					.withUsername("clinic")
					.withPassword("clinic");
				GenericContainer<?> app = new GenericContainer<>(
						new ImageFromDockerfile("vaga-viva-clinic-test", true).withDockerfile(DOCKERFILE.toAbsolutePath()))
					.withNetwork(network)
					.withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/clinic")
					.withEnv("SPRING_DATASOURCE_USERNAME", "clinic")
					.withEnv("SPRING_DATASOURCE_PASSWORD", "clinic")
					.withExposedPorts(8080)
					.dependsOn(database)
					.waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200)
						.withStartupTimeout(Duration.ofMinutes(3)))) {

			assertThatCode(app::start)
				.as("esperava construir a imagem do Dockerfile e ver a aplicação responder 200 em /actuator/health "
						+ "na porta 8080, usando as variáveis SPRING_DATASOURCE_*")
				.doesNotThrowAnyException();

			URI health = URI.create("http://" + app.getHost() + ":" + app.getMappedPort(8080) + "/actuator/health");
			HttpResponse<String> response = HttpClient.newHttpClient()
				.send(HttpRequest.newBuilder(health).build(), HttpResponse.BodyHandlers.ofString());
			assertThat(response.body()).as("esperava status UP em /actuator/health dentro do contêiner")
				.contains("\"UP\"");
		}
	}

	@Test
	@DisplayName("o compose.yaml declara a aplicação, construída do Dockerfile, e o PostgreSQL 17")
	void composeDeclaresAppAndDatabase() {
		Map<?, ?> services = services();

		assertThat(appService(services)).as("esperava em " + COMPOSE + " um serviço com 'build' para a aplicação")
			.isPresent();
		assertThat(databaseService(services))
			.as("esperava em " + COMPOSE + " um serviço com 'image: postgres:17' (ou uma variante, como postgres:17-alpine)")
			.isPresent();
	}

	@Test
	@DisplayName("o compose.yaml publica a porta 8080 e liga a aplicação ao banco pelo nome do serviço")
	void composeWiresAppToDatabase() {
		Map<?, ?> services = services();
		Map<?, ?> app = appService(services).map(Map.Entry::getValue).map(Map.class::cast).orElse(Map.of());
		String databaseName = databaseService(services).map(Map.Entry::getKey).map(String::valueOf).orElse("?");

		assertThat(list(app.get("ports"))).as("esperava a porta 8080 da aplicação em 'ports'")
			.anySatisfy((port) -> assertThat(port).contains("8080"));
		assertThat(environment(app).get("SPRING_DATASOURCE_URL"))
			.as("esperava SPRING_DATASOURCE_URL apontando para o serviço do banco ('" + databaseName + "')")
			.isNotNull()
			.contains("//" + databaseName + ":");
	}

	private static Map<?, ?> services() {
		assertThat(Files.isRegularFile(COMPOSE)).as("esperava encontrar o arquivo " + COMPOSE + " na raiz do projeto")
			.isTrue();
		try {
			Object yaml = new Yaml().load(Files.readString(COMPOSE));
			Object services = yaml instanceof Map<?, ?> map ? map.get("services") : null;
			assertThat(services).as("esperava a chave 'services' em " + COMPOSE).isInstanceOf(Map.class);
			return (Map<?, ?>) services;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static Optional<? extends Map.Entry<?, ?>> appService(Map<?, ?> services) {
		return services.entrySet()
			.stream()
			.filter((entry) -> entry.getValue() instanceof Map<?, ?> service && service.containsKey("build"))
			.findFirst();
	}

	private static Optional<? extends Map.Entry<?, ?>> databaseService(Map<?, ?> services) {
		return services.entrySet()
			.stream()
			.filter((entry) -> entry.getValue() instanceof Map<?, ?> service
					&& String.valueOf(service.get("image")).matches("(docker\\.io/)?(library/)?postgres:17.*"))
			.findFirst();
	}

	private static List<String> list(Object value) {
		List<String> items = new ArrayList<>();
		if (value instanceof Collection<?> collection) {
			collection.forEach((item) -> items.add(String.valueOf(item)));
		}
		return items;
	}

	private static Map<String, String> environment(Map<?, ?> service) {
		Map<String, String> environment = new java.util.HashMap<>();
		Object value = service.get("environment");
		if (value instanceof Map<?, ?> map) {
			map.forEach((key, val) -> environment.put(String.valueOf(key), String.valueOf(val)));
		}
		else if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				String[] parts = String.valueOf(item).split("=", 2);
				environment.put(parts[0], parts.length > 1 ? parts[1] : "");
			}
		}
		return environment;
	}

}
