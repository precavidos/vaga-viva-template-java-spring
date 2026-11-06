package io.github.precavidos.clinic.vagaviva;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Plano do Vaga Viva (.vaga-viva/plan.json)")
class MissionPlanConditionTest {

	@TempDir
	Path projectRoot;

	@Test
	@DisplayName("sem o arquivo de plano, todas as missões rodam")
	void withoutPlanEverythingRuns() {
		assertThat(evaluate("pagination").isDisabled()).as("esperava a missão ativa sem plan.json").isFalse();
	}

	@Test
	@DisplayName("desativa, com o motivo, as missões fora do plano")
	void disablesMissionsOutsideThePlan() throws IOException {
		writePlan("""
				{"archetype": "java-spring", "domain": "clinic-scheduling", "missions": ["patients-api", "persistence"]}
				""");

		ConditionEvaluationResult result = evaluate("pagination");

		assertThat(result.isDisabled()).as("esperava a missão pagination desativada").isTrue();
		assertThat(result.getReason()).as("esperava o motivo da desativação")
			.hasValue("Missão 'pagination' fora do seu plano (.vaga-viva/plan.json)");
	}

	@Test
	@DisplayName("mantém ativas as missões do plano e os testes sem missão")
	void keepsPlannedMissionsAndUntaggedTests() throws IOException {
		writePlan("""
				{"missions": ["patients-api"]}
				""");

		assertThat(evaluate("patients-api").isDisabled()).as("esperava patients-api ativa").isFalse();
		assertThat(MissionPlanCondition.evaluate(projectRoot, Set.of()).isDisabled())
			.as("esperava ativo um teste sem tag")
			.isFalse();
		assertThat(evaluate("slow").isDisabled()).as("esperava ativo um teste com tag que não é missão").isFalse();
	}

	@Test
	@DisplayName("recusa um arquivo de plano malformado com mensagem clara")
	void rejectsMalformedPlan() throws IOException {
		writePlan("{\"missions\": \"patients-api\"}");

		assertThatThrownBy(() -> evaluate("patients-api")).isInstanceOf(ExtensionConfigurationException.class)
			.hasMessageContaining("missions");
	}

	private ConditionEvaluationResult evaluate(String tag) {
		return MissionPlanCondition.evaluate(projectRoot, Set.of(tag));
	}

	private void writePlan(String json) throws IOException {
		Path plan = projectRoot.resolve(MissionPlanCondition.PLAN_FILE);
		Files.createDirectories(plan.getParent());
		Files.writeString(plan, json);
	}

}
