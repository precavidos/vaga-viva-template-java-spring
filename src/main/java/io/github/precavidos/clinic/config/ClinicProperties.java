package io.github.precavidos.clinic.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("clinic")
public record ClinicProperties(ZoneId timeZone, OpeningHours openingHours) {

	public record OpeningHours(Set<DayOfWeek> days, LocalTime opensAt, LocalTime closesAt) {
	}

}
