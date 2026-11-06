package io.github.precavidos.clinic.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class RecordingSendersConfiguration {

	@Bean
	@Primary
	RecordingEmailSender recordingEmailSender() {
		return new RecordingEmailSender();
	}

	@Bean
	@Primary
	RecordingSmsSender recordingSmsSender() {
		return new RecordingSmsSender();
	}

}
