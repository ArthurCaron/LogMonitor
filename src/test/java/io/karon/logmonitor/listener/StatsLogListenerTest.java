package io.karon.logmonitor.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.outputter.StatsOutputter;
import io.karon.logmonitor.LogGenerator;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatsLogListenerTest {
	private StatsOutputter statsOutputter;
	private StatsLogListener statsLogListener;

	private long initialDelay;
	private long period;
	private TimeUnit timeUnit;
	private int numberOfSectionsToShow;

	@BeforeAll
	void beforeAll() {
		initialDelay = 0;
		period = 1;
		timeUnit = TimeUnit.MINUTES;
		numberOfSectionsToShow = 3;
	}

	@BeforeEach
	void beforeEach() {
		statsOutputter = mock(StatsOutputter.class);
		statsLogListener = spy(new StatsLogListener(
				statsOutputter,
				initialDelay,
				period,
				timeUnit,
				numberOfSectionsToShow
		));
	}

	@Test
	void logOutsideDurationToMonitorCalledTest() {
		AccessLog oldAccessLog = LogGenerator.generateLog(ZonedDateTime.now().minusDays(1));

		statsLogListener.onNext(oldAccessLog);

		verify(statsOutputter).logOutsideDurationToMonitor(oldAccessLog);
	}

	@Test
	void sectionHitsNotEnoughSectionsTest() {
		Map<String, Integer> sectionsHits = new HashMap<>();

		AccessLog accessLog = LogGenerator.generateLog("/home");

		sectionsHits.put(accessLog.getSection(), 1);

		List<Map.Entry<String, Integer>> sectionsWithTheMostHits = new ArrayList<>(sectionsHits.entrySet());

		statsLogListener.onNext(accessLog);
		statsLogListener.runScheduledProcess();

		verify(statsOutputter).sectionHits(
				sectionsWithTheMostHits,
				period,
				timeUnit,
				statsLogListener.getEarliestInstantAtSystemDefaultClockZoneId()
		);
	}

	@Test
	void sectionHitsTest() {
		AccessLog accessLog1 = LogGenerator.generateLog("/home");
		AccessLog accessLog2 = LogGenerator.generateLog("/home2");
		AccessLog accessLog3 = LogGenerator.generateLog("/home3");
		AccessLog accessLog4 = LogGenerator.generateLog("/home4");

		Map.Entry<String,Integer> entry2 = new AbstractMap.SimpleEntry<>(accessLog2.getSection(), 2);
		Map.Entry<String,Integer> entry3 = new AbstractMap.SimpleEntry<>(accessLog3.getSection(), 3);
		Map.Entry<String,Integer> entry4 = new AbstractMap.SimpleEntry<>(accessLog4.getSection(), 4);

		List<Map.Entry<String, Integer>> sectionsWithTheMostHits = new ArrayList<>();
		sectionsWithTheMostHits.add(entry4);
		sectionsWithTheMostHits.add(entry3);
		sectionsWithTheMostHits.add(entry2);

		addXTimes(accessLog3, 3);
		addXTimes(accessLog2, 2);
		addXTimes(accessLog1, 1);
		addXTimes(accessLog4, 4);

		statsLogListener.runScheduledProcess();

		verify(statsOutputter).sectionHits(
				sectionsWithTheMostHits,
				period,
				timeUnit,
				statsLogListener.getEarliestInstantAtSystemDefaultClockZoneId()
		);
	}

	private void addXTimes(AccessLog accessLog, int times) {
		for (int i = 0; i < times; ++i) {
			statsLogListener.onNext(accessLog);
		}
	}
}
