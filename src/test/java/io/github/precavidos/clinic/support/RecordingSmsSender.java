package io.github.precavidos.clinic.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.precavidos.clinic.reminder.SmsSender;

public class RecordingSmsSender implements SmsSender {

	private final List<SentSms> sent = new CopyOnWriteArrayList<>();

	@Override
	public void send(String phone, String text) {
		sent.add(new SentSms(phone, text));
	}

	public List<SentSms> sent() {
		return List.copyOf(sent);
	}

	public void clear() {
		sent.clear();
	}

	public record SentSms(String phone, String text) {
	}

}
