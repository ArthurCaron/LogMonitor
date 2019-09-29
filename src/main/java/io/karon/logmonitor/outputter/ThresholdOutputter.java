package io.karon.logmonitor.outputter;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import io.karon.logmonitor.log.AccessLog;


public interface ThresholdOutputter {
	void logOutsideDurationToMonitor(AccessLog accessLog);
	void previousThresholdsReached();
	void thresholdReached(
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now
	);
	void alertStillActive(
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now
	);
	void trafficRecovered(
			int currentCount,
			long thresholdForDurationToMonitor,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now
	);
}
