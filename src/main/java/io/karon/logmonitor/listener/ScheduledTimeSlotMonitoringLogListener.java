package io.karon.logmonitor.listener;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;


abstract class ScheduledTimeSlotMonitoringLogListener extends ScheduledLogListener {
	private final long durationToMonitorAsLong;
	private final Duration durationToMonitor;
	private Instant earliestInstant;
	private Instant latestInstant;

	ScheduledTimeSlotMonitoringLogListener(long initialDelay, long period, TimeUnit timeUnit) {
		this(initialDelay, period, period, timeUnit);
	}

	ScheduledTimeSlotMonitoringLogListener(long initialDelay, long period, long durationToMonitor, TimeUnit timeUnit) {
		super(initialDelay, period, timeUnit);

		if (durationToMonitor == 0) {
			throw new IllegalArgumentException("The duration to monitor should be greater than zero");
		}

		this.durationToMonitorAsLong = durationToMonitor;
		this.durationToMonitor = Duration.ofMillis(timeUnit.toMillis(this.durationToMonitorAsLong));
	}

	void updateTimeSlot() {
		this.earliestInstant = Instant.now();
		this.latestInstant = earliestInstant.minus(durationToMonitor);
	}

	boolean isTooOldForTimeSlot(Instant accessLogInstant) {
		return latestInstant.compareTo(accessLogInstant) > 0;
	}

	boolean isNotTooRecentForTimeSlot(Instant accessLogInstant) {
		return accessLogInstant.compareTo(earliestInstant) <= 0;
	}

	ZonedDateTime getEarliestInstantAtSystemDefaultClockZoneId() {
		return earliestInstant.atZone(Clock.systemDefaultZone().getZone());
	}

	Long getDurationToMonitor() { return durationToMonitorAsLong; }
}
