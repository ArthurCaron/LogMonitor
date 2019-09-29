package io.karon.logmonitor.outputter;

import static io.karon.logmonitor.outputter.ConsoleOutputter.LINE_SEPARATOR;
import static io.karon.logmonitor.outputter.ConsoleOutputter.LOG_SEPARATOR;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.karon.logmonitor.log.AccessLog;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThresholdToConsoleOutputterTest {
	@Mock
	private Logger logger;

	private ThresholdOutputter thresholdOutputter;
	private TimeUnit timeUnit;
	private long durationToMonitor;

	@BeforeAll
	void beforeAll() {
		MockitoAnnotations.initMocks(this);

		timeUnit = TimeUnit.SECONDS;
		durationToMonitor = TimeUnit.MINUTES.convert(120, timeUnit);
	}

	@BeforeEach
	void beforeEach() {
		thresholdOutputter = new ThresholdToConsoleOutputter(logger);
	}

	@Test
	final void testLogOutsideDurationToMonitor(){
		AccessLog accessLog = mock(AccessLog.class);

		doReturn("170.142.69.176 - james [14/Jul/2019:14:42:22 +0200] \"PUT /dashboard/lists/manual/333/dashboards HTTP/1.0\" 429 21")
				.when(accessLog).toString();

		thresholdOutputter.logOutsideDurationToMonitor(accessLog);

		verify(logger).warn("Received accessLog |> {} <| which has an Instant outside of the monitored duration", accessLog);
	}

	// This test is probably the only one we care about, since the other methods only log the information we pass on to them
	@Test
	final void testPreviousThresholdsReached(){
		long thresholdForDurationToMonitor = 50;
		ZonedDateTime zonedDateTime = ZonedDateTime.now();

		ZonedDateTime firstAlertStart = zonedDateTime.minusSeconds(40);
		ZonedDateTime firstAlertStop = zonedDateTime.minusSeconds(30);
		thresholdOutputter.thresholdReached(100, thresholdForDurationToMonitor, durationToMonitor, timeUnit, firstAlertStart);
		thresholdOutputter.trafficRecovered(25, thresholdForDurationToMonitor, durationToMonitor, timeUnit, firstAlertStop);

		ZonedDateTime secondAlertStart = zonedDateTime.minusSeconds(20);
		ZonedDateTime secondAlertStop = zonedDateTime.minusSeconds(10);
		thresholdOutputter.thresholdReached(110, thresholdForDurationToMonitor, durationToMonitor, timeUnit, secondAlertStart);
		thresholdOutputter.trafficRecovered(15, thresholdForDurationToMonitor, durationToMonitor, timeUnit, secondAlertStop);

		thresholdOutputter.previousThresholdsReached();

		String alert = "Previous alerts raised because the threshold was reached: " + LINE_SEPARATOR
				+ "Alert started at " + firstAlertStart + " and ended at " + firstAlertStop + LINE_SEPARATOR
				+ "Alert started at " + secondAlertStart + " and ended at " + secondAlertStop + LINE_SEPARATOR
				+ LOG_SEPARATOR;

		verify(logger).info(alert);
	}

	@Test
	final void testThresholdReached(){
		int currentCount = 100;
		long thresholdForDurationToMonitor = 50;
		ZonedDateTime now = ZonedDateTime.now();

		thresholdOutputter.thresholdReached(currentCount, thresholdForDurationToMonitor, durationToMonitor, timeUnit, now);

		verify(logger).info(
				"{} - hits/threshold = {}/{} in the last {} {}, triggered at {}" + LINE_SEPARATOR + LOG_SEPARATOR,
				"High traffic generated an alert",
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);
	}

	@Test
	final void testAlertStillActive(){
		int currentCount = 100;
		long thresholdForDurationToMonitor = 50;
		ZonedDateTime now = ZonedDateTime.now();

		thresholdOutputter.alertStillActive(currentCount, thresholdForDurationToMonitor, durationToMonitor, timeUnit, now);

		verify(logger).info(
				"{} - hits/threshold = {}/{} in the last {} {}, triggered at {}"
						+ LINE_SEPARATOR + LOG_SEPARATOR,
				"High traffic still active",
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);
	}

	@Test
	final void testTrafficRecovered(){
		int currentCount = 40;
		long thresholdForDurationToMonitor = 50;
		ZonedDateTime now = ZonedDateTime.now();

		thresholdOutputter.trafficRecovered(
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);

		verify(logger).info(
				"{} - hits/threshold = {}/{} in the last {} {}, triggered at {}"
						+ LINE_SEPARATOR + LOG_SEPARATOR,
				"Traffic recovered",
				currentCount,
				thresholdForDurationToMonitor,
				durationToMonitor,
				timeUnit,
				now
		);
	}
}
