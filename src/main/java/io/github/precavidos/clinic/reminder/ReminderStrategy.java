package io.github.precavidos.clinic.reminder;

public interface ReminderStrategy {

	ReminderChannel channel();

	void send(Reminder reminder);

}
