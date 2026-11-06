package io.github.precavidos.clinic.support;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import com.jayway.jsonpath.JsonPath;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiResponse {

	private final String request;

	private final MockHttpServletResponse response;

	ApiResponse(String request, MockHttpServletResponse response) {
		this.request = request;
		this.response = response;
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
	}

	public int status() {
		return response.getStatus();
	}

	public String header(String name) {
		return response.getHeader(name);
	}

	public String contentType() {
		return response.getContentType();
	}

	public String body() {
		try {
			return response.getContentAsString();
		}
		catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Lê um valor do corpo JSON. Devolve {@code null} quando o caminho não existe ou o corpo não é JSON, para que o
	 * teste falhe na asserção seguinte, com uma mensagem clara.
	 */
	public <T> T json(String path) {
		try {
			return JsonPath.read(body(), path);
		}
		catch (RuntimeException ex) {
			return null;
		}
	}

	public String describe() {
		return request + " respondeu " + status() + " com corpo: " + body();
	}

}
