package io.karon.logmonitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.karon.logmonitor.log.AccessLog;

//import java.util.concurrent.TimeUnit;
//import io.karon.logmonitor.configuration.Configuration;
//import io.reactivex.Observable;
//import io.reactivex.schedulers.Schedulers;


public class LogGenerator {
	// Previously used to simulate the log file being filled naturally
//	public static void main(String[] args) {
//		File fileToMonitor = Configuration.getConfigValues()
//				.getFileToMonitorConfiguration()
//				.getFileToMonitor();
//
//		Observable.interval(0, 10, TimeUnit.MILLISECONDS)
//				.subscribeOn(Schedulers.io())
//				.subscribe(ignored -> LogGenerator.generateLogToFile(fileToMonitor, false));
//
//		while (true) {}
//	}

	private static final String RFC931 = "-";
	private static final String PROTOCOL = "HTTP/1.0";
	private static final Random random = new Random();

	private LogGenerator() {}

	public static AccessLog generateLog() {
		return new AccessLog(
				generateIpAddress(),
				generateClientIdentd(),
				generateUserID(),
				generateZonedDateTime(),
				generateMethod(),
				generateEndpoint(),
				generateProtocol(),
				String.valueOf(generateResponseCode()),
				String.valueOf(generateContentSize())
		);
	}

	public static AccessLog generateLog(ZonedDateTime zonedDateTime) {
		return new AccessLog(
				generateIpAddress(),
				generateClientIdentd(),
				generateUserID(),
				AccessLog.DATE_TIME_FORMATTER.format(zonedDateTime),
				generateMethod(),
				generateEndpoint(),
				generateProtocol(),
				String.valueOf(generateResponseCode()),
				String.valueOf(generateContentSize())
		);
	}

	public static AccessLog generateLog(String endpoint) {
		return new AccessLog(
				generateIpAddress(),
				generateClientIdentd(),
				generateUserID(),
				generateZonedDateTime(),
				generateMethod(),
				endpoint,
				generateProtocol(),
				String.valueOf(generateResponseCode()),
				String.valueOf(generateContentSize())
		);
	}

	private static String generateLog(boolean generateBadLogLines) {
		if (generateBadLogLines && random.nextInt(10) == 0) {
			return "BAD LOG LINE";
		} else {
			return generateIpAddress() + " "
					+ generateClientIdentd() + " "
					+ generateUserID() + " "
					+ "[" + generateZonedDateTime() + "] "
					+ "\"" + generateMethod() + " " + generateEndpoint() + " " + generateProtocol() + "\" "
					+ generateResponseCode() + " "
					+ generateContentSize();
		}
	}

	private static void generateLogToFile(File fileToWriteTo, boolean generateBadLogLines) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToWriteTo, true))) {
			if (fileToWriteTo.length() > 0) {
				writer.newLine();
			}
			writer.append(generateLog(generateBadLogLines));
		}
	}

	private static String generateIpAddress() {
		return random.nextInt(256) + "."
				+ random.nextInt(256) + "."
				+ random.nextInt(256) + "."
				+ random.nextInt(256);
	}

	private static String generateClientIdentd() {
		return RFC931;
	}

	private static String generateUserID() {
		return UserId.randomValue();
	}

	private static String generateZonedDateTime() {
		return AccessLog.DATE_TIME_FORMATTER.format(ZonedDateTime.now());
	}

	private static String generateMethod() {
		return Method.randomValue();
	}

	private static String generateEndpoint() {
		return Endpoint.randomValue();
	}

	private static String generateProtocol() {
		return PROTOCOL;
	}

	private static int generateResponseCode() {
		return ResponseCode.randomValue();
	}

	private static int generateContentSize() {
		return random.nextInt(1_000);
	}

	public enum UserId {
		JAMES("james"),
		JILL("jill"),
		FRANK("frank"),
		MARY("mary");

		private static final List<UserId> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();

		private String value;

		UserId(String value) {
			this.value = value;
		}

		public static String randomValue()  {
			return VALUES.get(random.nextInt(SIZE)).value;
		}
	}

	public enum Method {
		GET("GET"),
		POST("POST"),
		PUT("PUT"),
		DELETE("DELETE"),
		PATCH("PATCH");

		private static final List<Method> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();

		private String value;

		Method(String value) {
			this.value = value;
		}

		public static String randomValue()  {
			return VALUES.get(random.nextInt(SIZE)).value;
		}
	}

	public enum Endpoint {
		CHECK_RUN("/check_run"),
		COMMENTS("/comments"),
		COMMENTS_ID("/comments/111"),
		DASHBOARD("/dashboard"),
		DASHBOARD_ID("/dashboard/222"),
		DASHBOARD_LIST_MANUAL("/dashboard/lists/manual"),
		DASHBOARD_LIST_MANUAL_ID("/dashboard/lists/manual/333"),
		DASHBOARD_LIST_MANUAL_ID_DASHBOARDS("/dashboard/lists/manual/333/dashboards"),
		GRAPH_EMBED("/graph/embed"),
		GRAPH_SNAPSHOT("/graph/snapshot"),
		EVENTS("/events"),
		HOSTS("/hosts"),
		HOME("/home");

		private static final List<Endpoint> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();

		private String value;

		Endpoint(String value) {
			this.value = value;
		}

		public static String randomValue()  {
			return VALUES.get(random.nextInt(SIZE)).value;
		}
	}

	public enum ResponseCode {
		OK(200),
		CREATED(201),
		ACCEPTED(202),
		NO_CONTENT(204),
		MOVED_PERMANENTLY(301),
		NOT_MODIFIED(304),
		UNAUTHORIZED(401),
		FORBIDDEN(403),
		NOT_FOUND(404),
		CONFLICT(409),
		PAYLOAD_TOO_LARGE(413),
		UNPROCESSABLE(422),
		TOO_MANY_REQUESTS(429),
		SERVER_ERROR(500);

		private static final List<ResponseCode> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();

		private int value;

		ResponseCode(int value) {
			this.value = value;
		}

		public static int randomValue()  {
			return VALUES.get(random.nextInt(SIZE)).value;
		}
	}
}
