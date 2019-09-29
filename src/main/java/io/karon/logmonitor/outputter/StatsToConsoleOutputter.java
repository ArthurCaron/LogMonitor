package io.karon.logmonitor.outputter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import io.karon.logmonitor.log.AccessLog;


/*
Possible improvement:
	- add, in the configuration file, the messages we actually care about
		(it would also us to disable logOutsideDurationToMonitor messages, for example)
 */
public class StatsToConsoleOutputter implements StatsOutputter, ConsoleOutputter {
	private final Logger logger;

	private final StringBuilder stringBuilder;

	public StatsToConsoleOutputter(Logger logger) {
		this.logger = logger;
		this.stringBuilder = new StringBuilder();
	}

	public void logOutsideDurationToMonitor(AccessLog accessLog) {
		logger.warn("Received accessLog |> {} <| which has an Instant outside of the monitored duration", accessLog);
	}

	public void sectionHits(
			List<Map.Entry<String, Integer>> sectionsWithTheMostHits,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now) {
		stringBuilder.append("Log Stats generated in the last ")
				.append(durationToMonitor)
				.append(" ")
				.append(timeUnit)
				.append(" at ")
				.append(now)
				.append(":")
				.append(LINE_SEPARATOR);

		if (sectionsWithTheMostHits.isEmpty()) {
			stringBuilder.append("No logs received in the last ")
					.append(durationToMonitor)
					.append(" ")
					.append(timeUnit)
					.append(LINE_SEPARATOR);
		} else {
			stringBuilder.append("Sections with the most hits: ")
					.append(LINE_SEPARATOR);
			for (Map.Entry<String, Integer> sectionsWithTheMostHit : sectionsWithTheMostHits) {
				stringBuilder.append(TABULATION)
						.append("* Section: ")
						.append(sectionsWithTheMostHit.getKey())
						.append(LINE_SEPARATOR)
						.append(TABULATION)
						.append(TABULATION)
						.append("- Hits: ")
						.append(sectionsWithTheMostHit.getValue())
						.append(LINE_SEPARATOR);
			}
		}

		stringBuilder.append(LOG_SEPARATOR);

		logger.info(stringBuilder.toString());

		clearStringBuilder();
	}

	// Reusing the stringBuilder is apparently more efficient than allocating a new one
	private void clearStringBuilder() {
		stringBuilder.setLength(0);
	}
}
