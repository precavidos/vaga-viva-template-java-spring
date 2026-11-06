package io.github.precavidos.clinic.reminder;

import java.time.LocalDateTime;

public record Reminder(String patientName, String patientEmail, String patientPhone, String professionalName,
		LocalDateTime start) {
}
