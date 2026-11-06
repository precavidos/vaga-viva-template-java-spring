package io.github.precavidos.clinic.support;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Dados inseridos direto no banco, nas tabelas que o código inicial já cria.
 */
public class Fixtures {

	private final JdbcTemplate jdbc;

	public Fixtures(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public long professional(String name, String specialty) {
		return jdbc.queryForObject("INSERT INTO professionals (name, specialty) VALUES (?, ?) RETURNING id", Long.class,
				name, specialty);
	}

	public long professional() {
		return professional("Dra. Helena Prado", "Clínica geral");
	}

	public long appointment(long professionalId, long patientId, LocalDateTime start, int durationMinutes) {
		return jdbc.queryForObject("""
				INSERT INTO appointments (professional_id, patient_id, starts_at, duration_minutes)
				VALUES (?, ?, ?, ?) RETURNING id
				""", Long.class, professionalId, patientId, Timestamp.valueOf(start), durationMinutes);
	}

	public int countAppointments() {
		return jdbc.queryForObject("SELECT count(*) FROM appointments", Integer.class);
	}

	public boolean tableExists(String table) {
		return Boolean.TRUE.equals(jdbc.queryForObject("""
				SELECT EXISTS (SELECT 1 FROM information_schema.tables
				               WHERE table_schema = current_schema() AND table_name = ?)
				""", Boolean.class, table));
	}

}
