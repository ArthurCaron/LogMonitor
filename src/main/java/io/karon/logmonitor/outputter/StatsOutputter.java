package io.karon.logmonitor.outputter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.karon.logmonitor.log.AccessLog;


public interface StatsOutputter {
	void logOutsideDurationToMonitor(AccessLog accessLog);
	void sectionHits(
			List<Map.Entry<String, Integer>> sectionsWithTheMostHits,
			long durationToMonitor,
			TimeUnit timeUnit,
			ZonedDateTime now
	);
}
