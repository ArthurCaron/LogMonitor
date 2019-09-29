package io.karon.logmonitor.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.outputter.ThresholdOutputter;
import io.karon.logmonitor.LogGenerator;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThresholdLogListenerTest {
	private ThresholdOutputter thresholdOutputter;
	private ThresholdLogListener thresholdLogListener;

	private long initialDelay;
	private long period;
	private long durationToMonitor;
	private int threshold;
	private TimeUnit timeUnit;

	private long thresholdForDurationToMonitor;

	private int currentCount;
	private List<AccessLog> accessLogs;

	@BeforeAll
	void beforeAll() {
		initialDelay = 0;
		period = 1;
		durationToMonitor = 5;
		threshold = 1;
		timeUnit = TimeUnit.MINUTES;

		thresholdForDurationToMonitor = threshold * durationToMonitor;

		currentCount = 0;
		accessLogs = new ArrayList<>();

		while (currentCount <= thresholdForDurationToMonitor) {
			accessLogs.add(LogGenerator.generateLog());
			++currentCount;
		}
	}

	@BeforeEach
	void beforeEach() {
		thresholdOutputter = mock(ThresholdOutputter.class);
		thresholdLogListener = spy(new ThresholdLogListener(
				thresholdOutputter,
				initialDelay,
				period,
				durationToMonitor,
				threshold,
				timeUnit
		));
	}

	@Test
	void logOutsideDurationToMonitorCalledTest() {
		AccessLog oldAccessLog = LogGenerator.generateLog(ZonedDateTime.now().minusDays(1));

		thresholdLogListener.onNext(oldAccessLog);

		verify(thresholdOutputter).logOutsideDurationToMonitor(oldAccessLog);
	}

	@Test
	void previousThresholdsReachedCalledTest() {
		accessLogs.forEach(thresholdLogListener::onNext);
		thresholdLogListener.runScheduledProcess();
		thresholdLogListener.runScheduledProcess();
		thresholdLogListener.runScheduledProcess();

		verify(thresholdOutputter, times(3)).previousThresholdsReached();
	}

	@Test
	void thresholdReachedCalledTest() {
		accessLogs.forEach(thresholdLogListener::onNext);
		thresholdLogListener.runScheduledProcess();

		verify(thresholdOutputter).thresholdReached(
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				thresholdLogListener.getEarliestInstantAtSystemDefaultClockZoneId()
		);
	}

	@Test
	void alertStillActiveCalledTest() {
		accessLogs.forEach(thresholdLogListener::onNext);
		thresholdLogListener.runScheduledProcess();
		thresholdLogListener.runScheduledProcess();

		verify(thresholdOutputter).alertStillActive(
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				thresholdLogListener.getEarliestInstantAtSystemDefaultClockZoneId()
		);
	}

	@Test
	void trafficRecoveredCalledTest() {
		accessLogs.forEach(thresholdLogListener::onNext);
		thresholdLogListener.runScheduledProcess();

		// Disqualify all of the accessLogs as if they were all outside the durationToMonitor
		doReturn(true).when(thresholdLogListener).isTooOldForTimeSlot(any(Instant.class));
		thresholdLogListener.runScheduledProcess();

		verify(thresholdOutputter).trafficRecovered(
				0,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				thresholdLogListener.getEarliestInstantAtSystemDefaultClockZoneId()
		);
	}
}
