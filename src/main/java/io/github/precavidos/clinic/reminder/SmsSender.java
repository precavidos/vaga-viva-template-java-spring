package io.github.precavidos.clinic.reminder;

public interface SmsSender {

	void send(String phone, String text);

}
