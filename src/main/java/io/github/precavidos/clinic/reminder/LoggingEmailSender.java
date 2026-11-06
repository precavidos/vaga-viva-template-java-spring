package io.github.precavidos.clinic.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

	@Override
	public void send(String to, String subject, String body) {
		log.info("E-mail para {}: {} - {}", to, subject, body);
	}

}
