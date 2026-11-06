package io.github.precavidos.clinic.appointment;

import java.time.Duration;
import java.time.LocalDateTime;

public final class AppointmentDuration {

	private AppointmentDuration() {
	}

	public static int between(LocalDateTime start, LocalDateTime end) {
		return (int) Duration.between(start.toLocalTime(), end.toLocalTime()).toMinutes();
	}

}
