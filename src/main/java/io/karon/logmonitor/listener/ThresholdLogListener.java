package io.karon.logmonitor.listener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.karon.logmonitor.configuration.Configuration;
import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.outputter.ThresholdOutputter;


/*
Possible improvement to reduce the amount of data stored:
	Use a RingBuffer containing "time slots" as indexes and an accumulator as value (for instance, each box in the RingBuffer represents 10 seconds)
	The time slots are not really indexes, but we create an Instant representing the oldest time slot, and we can calculate which time slot corresponds to which index
	The RingBuffer can have a fixed size equal to either:
		- the durationToMonitor / period
		- the durationToMonitor / precision (precision being a new argument for the constructor, representing the precision as the size of the time slots)
	When we receive a new AccessLog, we calculate the time slot it should go in and increment the accumulator
	Every time the period is reached, we can easily update the oldest time slot variable and purge the RingBuffer of the time slots that are too old
	Then we just need to go through the RingBuffer, adding the values of the accumulators

This solution would make the code harder to read, and since I hadn't been given any big performance/memory imperative, I decided not to implement it yet.
 */

/*
What I wanted to add:
	- for each alerting, the possibility to choose the log level
	- a "low threshold reached", meaning if we go below some threshold we alert (it's weird if all of a sudden we go from 50 000 calls per second to 0)
	- specify if the alerting is done for all access logs or only for a specific section/sections or response code/codes (if we only care about 5XX errors on /login for instance)
 */
public class ThresholdLogListener extends ScheduledTimeSlotMonitoringLogListener {
	private final ThresholdOutputter thresholdOutputter;

	private final long thresholdForDurationToMonitor;

	private final List<Instant> accessLogInstants;
	private boolean thresholdReachedEarlier;

	public ThresholdLogListener(
			ThresholdOutputter thresholdOutputter,
			Configuration.ThresholdLogListenerConfiguration configuration) {
		this(
				thresholdOutputter,
				configuration.getInitialDelay(),
				configuration.getPeriod(),
				configuration.getDurationToMonitor(),
				configuration.getThreshold(),
				configuration.getTimeUnit()
		);
	}

	public ThresholdLogListener(
			ThresholdOutputter thresholdOutputter,
			long initialDelay,
			long period,
			long durationToMonitor,
			int threshold,
			TimeUnit timeUnit) {
		super(initialDelay, period, durationToMonitor, timeUnit);
		this.thresholdOutputter = thresholdOutputter;

		this.thresholdForDurationToMonitor = threshold * durationToMonitor;
		this.accessLogInstants = new ArrayList<>();
		this.thresholdReachedEarlier = false;
	}

	@Override
	void runScheduledProcess() { // TODO: replace by Observable.interval(...)
		int currentCount = calculateCurrentCount();
		boolean thresholdReached = currentCount > thresholdForDurationToMonitor;

		if (thresholdReached) {
			if (!thresholdReachedEarlier) {
				thresholdOutputter.thresholdReached(
						currentCount,
						thresholdForDurationToMonitor,
						getDurationToMonitor(),
						getTimeUnit(),
						getEarliestInstantAtSystemDefaultClockZoneId()
				);
				thresholdReachedEarlier = true;
			} else {
				thresholdOutputter.alertStillActive(
						currentCount,
						thresholdForDurationToMonitor,
						getDurationToMonitor(),
						getTimeUnit(),
						getEarliestInstantAtSystemDefaultClockZoneId()
				);
			}
		} else {
			if (thresholdReachedEarlier) {
				thresholdOutputter.trafficRecovered(
						currentCount,
						thresholdForDurationToMonitor,
						getDurationToMonitor(),
						getTimeUnit(),
						getEarliestInstantAtSystemDefaultClockZoneId()
				);
				thresholdReachedEarlier = false;
			}
		}

		thresholdOutputter.previousThresholdsReached();
	}

	@Override
	public void onNext(AccessLog accessLog) {
		if (accessLog != null) {
			updateTimeSlot();

			if (isTooOldForTimeSlot(accessLog.getInstant())) {
				thresholdOutputter.logOutsideDurationToMonitor(accessLog);
			} else {
				accessLogInstants.add(accessLog.getInstant());
			}
		}
	}

	// I don't like doing two things at once (removing + computing the data) but it's more efficient
	private int calculateCurrentCount() {
		updateTimeSlot();
		int currentCount = 0;

		Iterator<Instant> accessLogInstantsIterator = accessLogInstants.iterator();

		while (accessLogInstantsIterator.hasNext()) {
			Instant accessLogInstant = accessLogInstantsIterator.next();

			if (isTooOldForTimeSlot(accessLogInstant)) {
				accessLogInstantsIterator.remove();
			} else if (isNotTooRecentForTimeSlot(accessLogInstant)) {
				++currentCount;
			}
		}

		return currentCount;
	}
}
