package io.github.precavidos.clinic.appointment;

import java.time.LocalDateTime;

import io.github.precavidos.clinic.professional.Professional;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "professional_id")
	private Professional professional;

	@Column(name = "patient_id", nullable = false)
	private Long patientId;

	@Column(name = "starts_at", nullable = false)
	private LocalDateTime start;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes;

	protected Appointment() {
	}

	public Appointment(Professional professional, Long patientId, LocalDateTime start, LocalDateTime end) {
		this.professional = professional;
		this.patientId = patientId;
		this.start = start;
		this.durationMinutes = AppointmentDuration.between(start, end);
	}

	public Long getId() {
		return id;
	}

	public Professional getProfessional() {
		return professional;
	}

	public Long getPatientId() {
		return patientId;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public LocalDateTime getEnd() {
		return start.plusMinutes(durationMinutes);
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

}
