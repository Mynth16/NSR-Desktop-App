package application.models;

import java.time.LocalDateTime;

public class AuditTrail {
	private final LocalDateTime dateTime;
	private final String user;
	private final String type;
	private final String action;
	private final String details;

	public AuditTrail(LocalDateTime dateTime, String user, String type, String action, String details) {
		this.dateTime = dateTime;
		this.user = user;
		this.type = type;
		this.action = action;
		this.details = details;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public String getUser() {
		return user;
	}

	public String getType() {
		return type;
	}

	public String getAction() {
		return action;
	}

	public String getDetails() {
		return details;
	}
}
