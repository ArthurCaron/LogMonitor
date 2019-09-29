package io.karon.logmonitor.outputter;

import static io.karon.logmonitor.outputter.ConsoleOutputter.LINE_SEPARATOR;
import static io.karon.logmonitor.outputter.ConsoleOutputter.LOG_SEPARATOR;
import static io.karon.logmonitor.outputter.ConsoleOutputter.TABULATION;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.karon.logmonitor.LogGenerator;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatsToConsoleOutputterTest {
	@Mock
	private Logger logger;

	private TimeUnit timeUnit;
	private long durationToMonitor;
	private StatsOutputter statsOutputter;

	private List<Map.Entry<String, Integer>> sectionsWithTheMostHits;

	@BeforeAll
	void beforeAll() {
		MockitoAnnotations.initMocks(this);

		timeUnit = TimeUnit.SECONDS;
		durationToMonitor = TimeUnit.SECONDS.convert(10, timeUnit);
		statsOutputter = new StatsToConsoleOutputter(logger);
	}

	@BeforeEach
	void beforeEach() {
		sectionsWithTheMostHits = new ArrayList<>();
	}

	@Test
	void sectionHitsTest() {
		Map.Entry<String,Integer> entry1 = new AbstractMap.SimpleEntry<>(LogGenerator.generateLog().getSection(), 10);
		Map.Entry<String,Integer> entry2 = new AbstractMap.SimpleEntry<>(LogGenerator.generateLog().getSection(), 2);
		Map.Entry<String,Integer> entry3 = new AbstractMap.SimpleEntry<>(LogGenerator.generateLog().getSection(), 15);

		sectionsWithTheMostHits.add(entry1);
		sectionsWithTheMostHits.add(entry2);
		sectionsWithTheMostHits.add(entry3);

		ZonedDateTime now = ZonedDateTime.now();

		statsOutputter.sectionHits(sectionsWithTheMostHits, durationToMonitor, timeUnit, now);

		String sectionHitsLog = "Log Stats generated "
				+ "in the last " + durationToMonitor + " " + timeUnit
				+ " at " + now + ":" + LINE_SEPARATOR
				+ "Sections with the most hits: " + LINE_SEPARATOR
				+ TABULATION + "* Section: " + entry1.getKey() + LINE_SEPARATOR
				+ TABULATION + TABULATION + "- Hits: " + entry1.getValue() + LINE_SEPARATOR
				+ TABULATION + "* Section: " + entry2.getKey() + LINE_SEPARATOR
				+ TABULATION + TABULATION + "- Hits: " + entry2.getValue() + LINE_SEPARATOR
				+ TABULATION + "* Section: " + entry3.getKey() + LINE_SEPARATOR
				+ TABULATION + TABULATION + "- Hits: " + entry3.getValue() + LINE_SEPARATOR
				+ LOG_SEPARATOR;

		verify(logger).info(sectionHitsLog);
	}

	@Test
	void emptySectionHitsTest() {
		ZonedDateTime now = ZonedDateTime.now();

		statsOutputter.sectionHits(sectionsWithTheMostHits, durationToMonitor, timeUnit, now);

		String emptySectionHitsLog = "Log Stats generated "
				+ "in the last " + durationToMonitor + " " + timeUnit
				+ " at " + now + ":" + LINE_SEPARATOR
				+ "No logs received in the last " + durationToMonitor + " " + timeUnit + LINE_SEPARATOR
				+ LOG_SEPARATOR;

		verify(logger).info(emptySectionHitsLog);
	}
}
