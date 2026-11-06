package io.github.precavidos.clinic.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * Datas relativas ao relógio real, no fuso da clínica.
 */
public final class ClinicDates {

	public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

	private ClinicDates() {
	}

	public static LocalDate today() {
		return LocalDate.now(ZONE);
	}

	/** Primeiro dia da semana pedido que esteja pelo menos 7 dias no futuro. */
	public static LocalDate future(DayOfWeek day) {
		return today().plusDays(7).with(TemporalAdjusters.nextOrSame(day));
	}

	/** Dia da semana pedido, pelo menos 7 dias no passado. */
	public static LocalDate past(DayOfWeek day) {
		return today().minusDays(7).with(TemporalAdjusters.previousOrSame(day));
	}

	public static LocalDateTime at(LocalDate date, String time) {
		return LocalDateTime.of(date, LocalTime.parse(time));
	}

}
