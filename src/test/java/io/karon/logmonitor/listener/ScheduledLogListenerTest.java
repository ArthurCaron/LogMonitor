package io.karon.logmonitor.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.karon.logmonitor.outputter.ThresholdOutputter;
import io.karon.logmonitor.LogGenerator;
import io.reactivex.schedulers.TestScheduler;


class ScheduledLogListenerTest {
	@Test
	void schedulerTest() {
		long period = 1;
		TimeUnit timeUnit = TimeUnit.SECONDS;

		ThresholdLogListener thresholdLogListener = spy(new ThresholdLogListener(
				mock(ThresholdOutputter.class),
				0,
				period,
				5,
				1,
				timeUnit
		));

		TestScheduler testScheduler = new TestScheduler();
		thresholdLogListener.startScheduler(testScheduler);
		thresholdLogListener.onNext(LogGenerator.generateLog());

		testScheduler.advanceTimeBy(period, timeUnit);

		// The timeout(100) waits for the scheduler to actually call runScheduledProcess()
		// Unfortunately, to make it work I had to add junit:junit:4.2 to my pom.xml
		// Testing threaded code probably wasn't the smartest idea :)
		verify(thresholdLogListener, timeout(100)).runScheduledProcess();

		// Another option which doesn't require the dependency, but doesn't seem to work all the time:
//		Assertions.assertTimeout(Duration.ofMillis(100), () -> verify(thresholdLogListener).runScheduledProcess());
	}
}
