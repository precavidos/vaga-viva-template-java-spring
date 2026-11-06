package io.github.precavidos.clinic.reminder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingSmsSender implements SmsSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

	@Override
	public void send(String phone, String text) {
		log.info("SMS para {}: {}", phone, text);
	}

}
