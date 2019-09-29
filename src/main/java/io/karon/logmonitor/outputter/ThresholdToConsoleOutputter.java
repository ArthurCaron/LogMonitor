package io.karon.logmonitor.outputter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import io.karon.logmonitor.log.AccessLog;


/*
Possible improvement:
	- add, in the configuration file, the messages we actually care about
		(it would also us to disable logOutsideDurationToMonitor messages, for example)
 */
public class ThresholdToConsoleOutputter implements ThresholdOutputter, ConsoleOutputter {
	private final Logger logger;

	private final StringBuilder stringBuilder;
	private final List<GeneratedAlert> generatedAlerts;

	private GeneratedAlert currentAlert;

	public ThresholdToConsoleOutputter(Logger logger) {
		this.logger = logger;
		this.stringBuilder = new StringBuilder();
		this.generatedAlerts = new ArrayList<>();
	}

	@Override
	public void logOutsideDurationToMonitor(AccessLog accessLog) {
		logger.warn("Received accessLog |> {} <| which has an Instant outside of the monitored duration", accessLog);
	}

	@Override
	public void previousThresholdsReached() {
		if (!generatedAlerts.isEmpty()) {
			stringBuilder.append("Previous alerts raised because the threshold was reached: ")
					.append(LINE_SEPARATOR);

			for (GeneratedAlert generatedAlert : generatedAlerts) {
				stringBuilder.append("Alert started at ")
						.append(generatedAlert.started)
						.append(" and ended at ")
						.append(generatedAlert.finished)
						.append(LINE_SEPARATOR);
			}

			stringBuilder.append(LOG_SEPARATOR);

			logger.info(stringBuilder.toString());

			clearStringBuilder();
		}
	}

	@Override
	public void thresholdReached(
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now) {
		thresholdMessage(
				"High traffic generated an alert",
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);

		currentAlert = new GeneratedAlert(now);
	}

	@Override
	public void alertStillActive(
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now) {
		thresholdMessage(
				"High traffic still active",
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);
	}

	@Override
	public void trafficRecovered(
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now) {
		thresholdMessage(
				"Traffic recovered",
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);

		if (currentAlert == null) {
			currentAlert = new GeneratedAlert(now);
		}

		currentAlert.trafficRecovered(now);
		generatedAlerts.add(currentAlert);
	}

	// I modified the message requested slightly (adding "in the last {} {}" to it)
	private void thresholdMessage(
			String message,
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now) {
		logger.info(
				"{} - hits/threshold = {}/{} in the last {} {}, triggered at {}"
						+ LINE_SEPARATOR + LOG_SEPARATOR,
				message,
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);
	}

	// Reusing the stringBuilder is apparently more efficient than allocating a new one
	private void clearStringBuilder() {
		stringBuilder.setLength(0);
	}

	private class GeneratedAlert {
		ZonedDateTime started;
		ZonedDateTime finished;

		private GeneratedAlert(ZonedDateTime started) {
			this.started = started;
		}

		private void trafficRecovered(ZonedDateTime finished) {
			this.finished = finished;
		}
	}
}
