package io.github.precavidos.clinic.missions;

import static io.github.precavidos.clinic.support.ClinicDates.at;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import io.github.precavidos.clinic.reminder.ReminderChannel;
import io.github.precavidos.clinic.reminder.ReminderStrategy;
import io.github.precavidos.clinic.support.ApiResponse;
import io.github.precavidos.clinic.support.ClinicDates;
import io.github.precavidos.clinic.support.IntegrationTest;
import io.github.precavidos.clinic.support.Missions;
import io.github.precavidos.clinic.support.Patients;
import io.github.precavidos.clinic.support.Patients.PatientData;
import io.github.precavidos.clinic.support.RecordingEmailSender.SentEmail;
import io.github.precavidos.clinic.support.RecordingSmsSender.SentSms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Tag(Missions.REMINDER_STRATEGIES)
@DisplayName("Missão reminder-strategies: lembretes por e-mail e SMS")
class ReminderStrategiesTest extends IntegrationTest {

	private static final String PROFESSIONAL_NAME = "Dr. Rafael Moura";

	private final LocalDate monday = ClinicDates.future(DayOfWeek.MONDAY);

	private final String expectedDate = monday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

	@Autowired
	private ApplicationContext context;

	private PatientData patient;

	private long appointmentId;

	@BeforeEach
	void setUp() {
		patient = Patients.newPatient();
		long patientId = Patients.create(api, patient);
		long professionalId = fixtures.professional(PROFESSIONAL_NAME, "Cardiologia");
		appointmentId = fixtures.appointment(professionalId, patientId, at(monday, "10:15"), 30);
	}

	@Test
	@DisplayName("envia o lembrete por e-mail para o e-mail do paciente")
	void sendsEmailReminder() {
		ApiResponse response = api.post("/appointments/{id}/reminders?channel=EMAIL", null, appointmentId);

		assertThat(response.status())
			.as("esperava status 202 em POST /appointments/{id}/reminders?channel=EMAIL; " + response.describe())
			.isEqualTo(202);
		assertThat(emails.sent()).as("esperava exatamente um e-mail enviado pelo EmailSender").hasSize(1);
		SentEmail email = emails.sent().getFirst();
		assertThat(email.to()).as("esperava o e-mail enviado para o endereço do paciente").isEqualTo(patient.email());
		assertThat(email.subject()).as("esperava um assunto no e-mail").isNotBlank();
		assertThat(email.body()).as("esperava no texto do e-mail o profissional, a data (dd/MM/aaaa) e a hora (HH:mm)")
			.contains(PROFESSIONAL_NAME, expectedDate, "10:15");
		assertThat(sms.sent()).as("esperava que o lembrete por e-mail não enviasse SMS").isEmpty();
	}

	@Test
	@DisplayName("envia o lembrete por SMS para o telefone do paciente")
	void sendsSmsReminder() {
		ApiResponse response = api.post("/appointments/{id}/reminders?channel=SMS", null, appointmentId);

		assertThat(response.status())
			.as("esperava status 202 em POST /appointments/{id}/reminders?channel=SMS; " + response.describe())
			.isEqualTo(202);
		assertThat(sms.sent()).as("esperava exatamente um SMS enviado pelo SmsSender").hasSize(1);
		SentSms message = sms.sent().getFirst();
		assertThat(message.phone()).as("esperava o SMS enviado para o telefone do paciente").isEqualTo(patient.phone());
		assertThat(message.text()).as("esperava no SMS o profissional, a data (dd/MM/aaaa) e a hora (HH:mm)")
			.contains(PROFESSIONAL_NAME, expectedDate, "10:15");
		assertThat(emails.sent()).as("esperava que o lembrete por SMS não enviasse e-mail").isEmpty();
	}

	@Test
	@DisplayName("tem uma estratégia (ReminderStrategy) para cada canal")
	void oneStrategyPerChannel() {
		Collection<ReminderStrategy> strategies = context.getBeansOfType(ReminderStrategy.class).values();

		assertThat(strategies).as("esperava um bean de ReminderStrategy para cada valor de ReminderChannel")
			.extracting(ReminderStrategy::channel)
			.containsExactlyInAnyOrder(ReminderChannel.values());
		assertThat(strategies).as("esperava uma classe diferente para cada canal")
			.extracting((strategy) -> strategy.getClass())
			.doesNotHaveDuplicates();
	}

}
