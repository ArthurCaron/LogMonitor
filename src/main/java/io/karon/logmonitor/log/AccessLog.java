package io.karon.logmonitor.log;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * I found the original code here:
 * https://github.com/databricks/reference-apps/blob/master/logs_analyzer/chapter1/java8/src/main/java/com/databricks/apps/logs/ApacheAccessLog.java
 *
 * This class represents an Apache access log line.
 * See http://httpd.apache.org/docs/2.2/logs.html for more details.
 */
public class AccessLog implements Log {
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);

	private final String ipAddress;
	private final String clientIdentd;
	private final String userID;
	private final ZonedDateTime zonedDateTime;
	private final Instant instant;
	private final String method;
	private final String endpoint;
	private final String protocol;
	private final int responseCode;
	private final long contentSize;

	public AccessLog(String ipAddress,
			String clientIdentd,
			String userID,
			String zonedDateTime,
			String method,
			String endpoint,
			String protocol,
			String responseCode,
			String contentSize) {
		this.ipAddress = ipAddress;
		this.clientIdentd = clientIdentd;
		this.userID = userID;
		this.zonedDateTime = ZonedDateTime.parse(zonedDateTime, DATE_TIME_FORMATTER);
		this.instant = Instant.from(this.zonedDateTime);
		this.method = method;
		this.endpoint = endpoint;
		this.protocol = protocol;
		this.responseCode = Integer.parseInt(responseCode);
		this.contentSize = Long.parseLong(contentSize);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getClientIdentd() {
		return clientIdentd;
	}

	public String getUserID() {
		return userID;
	}

	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}

	public Instant getInstant() {
		return instant;
	}

	public String getMethod() {
		return method;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public String getSection() {
		return "/" + endpoint.split("/", 3)[1];
	}

	public String getProtocol() {
		return protocol;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public long getContentSize() {
		return contentSize;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s [%s] \"%s %s %s\" %s %s",
				ipAddress,
				clientIdentd,
				userID,
				DATE_TIME_FORMATTER.format(zonedDateTime),
				method,
				endpoint,
				protocol,
				responseCode,
				contentSize);
	}
}
