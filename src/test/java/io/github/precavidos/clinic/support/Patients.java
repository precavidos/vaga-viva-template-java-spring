package io.github.precavidos.clinic.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pacientes fictícios, cadastrados pela API da missão {@code patients-api}.
 */
public final class Patients {

	private static final AtomicInteger sequence = new AtomicInteger();

	private Patients() {
	}

	public static PatientData newPatient() {
		int n = sequence.incrementAndGet();
		return new PatientData("Paciente Teste " + n, "paciente" + n + "@example.com",
				String.format("+55 11 90000-%04d", n % 10000), "1990-05-17");
	}

	public static long create(Api api) {
		return create(api, newPatient());
	}

	public static long create(Api api, PatientData patient) {
		ApiResponse response = api.post("/patients", patient.toJson());
		assertThat(response.status())
			.as("esperava status 201 ao cadastrar um paciente em POST /patients (missão patients-api); "
					+ response.describe())
			.isEqualTo(201);
		Number id = response.json("$.id");
		assertThat(id).as("esperava o campo 'id' no corpo da resposta de POST /patients; " + response.describe())
			.isNotNull();
		return id.longValue();
	}

	public record PatientData(String name, String email, String phone, String birthDate) {

		public String toJson() {
			return """
					{"name": %s, "email": %s, "phone": %s, "birthDate": %s}
					""".formatted(quote(name), quote(email), quote(phone), quote(birthDate));
		}

		private static String quote(String value) {
			return value == null ? "null" : "\"" + value.replace("\"", "\\\"") + "\"";
		}

	}

}
