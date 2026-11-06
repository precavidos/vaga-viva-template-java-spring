package io.github.precavidos.clinic.support;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Cliente HTTP dos testes. Por padrão, toda chamada envia as credenciais da recepcionista ({@link TestUser}).
 */
public class Api {

	private final MockMvc mockMvc;

	private final String authorization;

	public Api(MockMvc mockMvc) {
		this(mockMvc, TestUser.RECEPTIONIST.basicAuthorization());
	}

	private Api(MockMvc mockMvc, String authorization) {
		this.mockMvc = mockMvc;
		this.authorization = authorization;
	}

	public Api as(TestUser user) {
		return new Api(mockMvc, user.basicAuthorization());
	}

	public Api withCredentials(String username, String password) {
		return new Api(mockMvc, TestUser.basicAuthorization(username, password));
	}

	public Api anonymous() {
		return new Api(mockMvc, null);
	}

	public ApiResponse get(String path, Object... uriVariables) {
		return perform(HttpMethod.GET, path, null, uriVariables);
	}

	public ApiResponse post(String path, String jsonBody, Object... uriVariables) {
		return perform(HttpMethod.POST, path, jsonBody, uriVariables);
	}

	public ApiResponse put(String path, String jsonBody, Object... uriVariables) {
		return perform(HttpMethod.PUT, path, jsonBody, uriVariables);
	}

	public ApiResponse delete(String path, Object... uriVariables) {
		return perform(HttpMethod.DELETE, path, null, uriVariables);
	}

	private ApiResponse perform(HttpMethod method, String path, String jsonBody, Object... uriVariables) {
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, path, uriVariables)
			.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON);
		if (authorization != null) {
			request.header(HttpHeaders.AUTHORIZATION, authorization);
		}
		if (jsonBody != null) {
			request.contentType(MediaType.APPLICATION_JSON).content(jsonBody);
		}
		try {
			return new ApiResponse(method + " " + path, mockMvc.perform(request).andReturn().getResponse());
		}
		catch (Exception ex) {
			throw new IllegalStateException("Falha ao chamar " + method + " " + path, ex);
		}
	}

}
