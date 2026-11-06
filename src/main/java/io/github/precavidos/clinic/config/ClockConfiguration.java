package io.github.precavidos.clinic.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ClockConfiguration {

	@Bean
	Clock clock(ClinicProperties properties) {
		return Clock.system(properties.timeZone());
	}

}
