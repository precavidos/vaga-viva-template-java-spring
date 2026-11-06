package io.github.precavidos.clinic.vagaviva;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Desativa os testes das missões que não estão em {@code .vaga-viva/plan.json}. Sem o arquivo, todos os testes rodam.
 * Registrada por autodetecção ({@code junit-platform.properties}).
 */
public class MissionPlanCondition implements ExecutionCondition {

	public static final Path PLAN_FILE = Path.of(".vaga-viva", "plan.json");

	private static final String PLAN_FILE_NAME = ".vaga-viva/plan.json";

	static final Set<String> MISSIONS = Set.of("patients-api", "persistence", "scheduling-rules", "input-validation",
			"bug-hunt", "pagination", "ci-pipeline", "availability-query", "containerization", "reminder-strategies",
			"api-security");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return evaluate(Path.of("").toAbsolutePath(), context.getTags());
	}

	static ConditionEvaluationResult evaluate(Path projectRoot, Set<String> tags) {
		Set<String> missionTags = new HashSet<>(tags);
		missionTags.retainAll(MISSIONS);
		if (missionTags.isEmpty()) {
			return ConditionEvaluationResult.enabled("teste sem missão: roda sempre");
		}
		Path planFile = projectRoot.resolve(PLAN_FILE);
		if (!Files.exists(planFile)) {
			return ConditionEvaluationResult.enabled("sem " + PLAN_FILE_NAME + ": todas as missões rodam");
		}
		Set<String> planned = readMissions(planFile);
		for (String mission : missionTags) {
			if (planned.contains(mission)) {
				return ConditionEvaluationResult.enabled("missão '" + mission + "' está no seu plano");
			}
		}
		return ConditionEvaluationResult
			.disabled("Missão '" + String.join("', '", missionTags) + "' fora do seu plano (" + PLAN_FILE_NAME + ")");
	}

	private static Set<String> readMissions(Path planFile) {
		try {
			JsonNode missions = JsonMapper.builder().build().readTree(Files.readString(planFile)).path("missions");
			if (!missions.isArray()) {
				throw new ExtensionConfigurationException(
						"O arquivo " + PLAN_FILE_NAME + " precisa ter a lista \"missions\" com os ids das missões");
			}
			Set<String> planned = new HashSet<>();
			missions.forEach((mission) -> planned.add(mission.asString()));
			return planned;
		}
		catch (IOException | JacksonException ex) {
			throw new ExtensionConfigurationException("Não foi possível ler " + PLAN_FILE_NAME + ": " + ex.getMessage(), ex);
		}
	}

}
