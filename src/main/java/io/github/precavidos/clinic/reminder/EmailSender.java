package io.github.precavidos.clinic.reminder;

public interface EmailSender {

	void send(String to, String subject, String body);

}
