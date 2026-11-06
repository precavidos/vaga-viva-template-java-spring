package io.github.precavidos.clinic.support;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base dos testes de integração: um único contexto do Spring, com PostgreSQL no Testcontainers, compartilhado por todos
 * os testes. Antes de cada teste, todas as tabelas são esvaziadas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, RecordingSendersConfiguration.class })
public abstract class IntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected RecordingEmailSender emails;

	@Autowired
	protected RecordingSmsSender sms;

	protected Api api;

	protected Fixtures fixtures;

	@BeforeEach
	void resetState() {
		List<String> tables = jdbc.queryForList("""
				SELECT quote_ident(tablename) FROM pg_tables
				WHERE schemaname = current_schema() AND tablename <> 'flyway_schema_history'
				""", String.class);
		if (!tables.isEmpty()) {
			jdbc.execute("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE");
		}
		emails.clear();
		sms.clear();
		api = new Api(mockMvc);
		fixtures = new Fixtures(jdbc);
	}

}
