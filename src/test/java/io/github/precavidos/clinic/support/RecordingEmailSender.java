package io.github.precavidos.clinic.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.precavidos.clinic.reminder.EmailSender;

public class RecordingEmailSender implements EmailSender {

	private final List<SentEmail> sent = new CopyOnWriteArrayList<>();

	@Override
	public void send(String to, String subject, String body) {
		sent.add(new SentEmail(to, subject, body));
	}

	public List<SentEmail> sent() {
		return List.copyOf(sent);
	}

	public void clear() {
		sent.clear();
	}

	public record SentEmail(String to, String subject, String body) {
	}

}
