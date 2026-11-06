package io.github.precavidos.clinic.missions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.precavidos.clinic.support.Missions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

@Tag(Missions.CI_PIPELINE)
@DisplayName("Missão ci-pipeline: pipeline de integração contínua")
class CiPipelineTest {

	private static final Path WORKFLOW = Path.of(".github", "workflows", "ci.yml");

	@Test
	@DisplayName("o workflow ci.yml dispara a cada push e a cada pull request")
	void triggersOnPushAndPullRequest() {
		Map<?, ?> workflow = loadWorkflow();

		Object on = workflow.containsKey("on") ? workflow.get("on") : workflow.get(Boolean.TRUE);

		assertThat(events(on)).as("esperava os eventos push e pull_request na chave 'on' de " + WORKFLOW)
			.contains("push", "pull_request");
	}

	@Test
	@DisplayName("o workflow usa Java 21 com cache do Maven")
	void usesJava21WithMavenCache() {
		List<Map<?, ?>> steps = steps(loadWorkflow());

		List<Map<?, ?>> setupJava = steps.stream()
			.filter((step) -> String.valueOf(step.get("uses")).startsWith("actions/setup-java"))
			.toList();

		assertThat(setupJava).as("esperava um passo com 'uses: actions/setup-java' em " + WORKFLOW).isNotEmpty();
		assertThat(setupJava).as("esperava o actions/setup-java configurado com java-version 21 e cache maven")
			.anySatisfy((step) -> {
				Map<?, ?> with = step.get("with") instanceof Map<?, ?> map ? map : Map.of();
				assertThat(String.valueOf(with.get("java-version"))).as("java-version").startsWith("21");
				assertThat(with.get("cache")).as("cache").isEqualTo("maven");
			});
	}

	@Test
	@DisplayName("o workflow roda ./mvnw verify")
	void runsMavenVerify() {
		List<Map<?, ?>> steps = steps(loadWorkflow());

		List<String> commands = steps.stream()
			.map((step) -> step.get("run"))
			.filter((run) -> run != null)
			.map(String::valueOf)
			.toList();

		assertThat(commands).as("esperava um passo 'run' que execute ./mvnw verify em " + WORKFLOW)
			.anySatisfy((command) -> assertThat(command).contains("./mvnw").contains("verify"));
	}

	private static Map<?, ?> loadWorkflow() {
		assertThat(Files.isRegularFile(WORKFLOW)).as("esperava encontrar o arquivo " + WORKFLOW + " na raiz do projeto")
			.isTrue();
		try {
			Object yaml = new Yaml().load(Files.readString(WORKFLOW));
			assertThat(yaml).as("esperava que " + WORKFLOW + " fosse um YAML com as chaves 'on' e 'jobs'")
				.isInstanceOf(Map.class);
			return (Map<?, ?>) yaml;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static Collection<String> events(Object on) {
		List<String> events = new ArrayList<>();
		if (on instanceof String event) {
			events.add(event);
		}
		else if (on instanceof Collection<?> list) {
			list.forEach((event) -> events.add(String.valueOf(event)));
		}
		else if (on instanceof Map<?, ?> map) {
			map.keySet().forEach((event) -> events.add(String.valueOf(event)));
		}
		return events;
	}

	private static List<Map<?, ?>> steps(Map<?, ?> workflow) {
		List<Map<?, ?>> steps = new ArrayList<>();
		if (workflow.get("jobs") instanceof Map<?, ?> jobs) {
			for (Object job : jobs.values()) {
				if (job instanceof Map<?, ?> jobMap && jobMap.get("steps") instanceof List<?> jobSteps) {
					jobSteps.stream().filter(Map.class::isInstance).map((step) -> (Map<?, ?>) step).forEach(steps::add);
				}
			}
		}
		assertThat(steps).as("esperava ao menos um job com 'steps' em " + WORKFLOW).isNotEmpty();
		return steps;
	}

}
