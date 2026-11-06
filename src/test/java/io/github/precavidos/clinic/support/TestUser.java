package io.github.precavidos.clinic.support;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Usuários de teste. Os mesmos dados estão em {@code src/test/resources/application-test.yaml}, na propriedade
 * {@code clinic.security.users}.
 */
public enum TestUser {

	RECEPTIONIST("recepcao", "recepcao-senha"),

	PROFESSIONAL("dra.helena", "profissional-senha");

	private final String username;

	private final String password;

	TestUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String username() {
		return username;
	}

	public String password() {
		return password;
	}

	public String basicAuthorization() {
		return basicAuthorization(username, password);
	}

	public static String basicAuthorization(String username, String password) {
		String credentials = username + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
	}

}
