package io.karon.logmonitor.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.karon.logmonitor.configuration.Configuration;
import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.outputter.StatsOutputter;


/*
We could simply store the information we care about (the section in this case) and the number of hits, reducing the memory cost of storing everything.
But it would increase the memory cost by creating new objects and we would risk giving wrong information if the scheduler is lagging behind.
And it would also make the code harder to read.
 */

/*
What I wanted to add:
	- stats by response code or packet size instead of section (among other stats we may care about)
	- filter by response code / section (if we only care about /login or 5XX errors for example)
	- maybe something that remembers the last time the scheduler was called, so we can inform if some stats are going up or down (more 5XX errors that earlier for example)
	-
 */
public class StatsLogListener extends ScheduledTimeSlotMonitoringLogListener {
	private final StatsOutputter statsOutputter;
	private final int numberOfSectionsToShow;

	private final List<AccessLog> accessLogs;

	public StatsLogListener(StatsOutputter statsOutputter, Configuration.StatsLogListenerConfiguration configuration) {
		this(
				statsOutputter,
				configuration.getInitialDelay(),
				configuration.getPeriod(),
				configuration.getTimeUnit(),
				configuration.getNumberOfSectionsToShow()
		);
	}

	public StatsLogListener(
			StatsOutputter statsOutputter,
			long initialDelay,
			long period,
			TimeUnit timeUnit,
			int numberOfSectionsToShow) {
		super(initialDelay, period, timeUnit);
		this.statsOutputter = statsOutputter;
		this.numberOfSectionsToShow = numberOfSectionsToShow;

		this.accessLogs = new ArrayList<>();
	}

	@Override
	void runScheduledProcess() { // TODO: replace by Observable.interval(...)
		Map<String, Integer> sectionsHits = computeSectionsHits();

		statsOutputter.sectionHits(
				computeSectionsWithTheMostHits(sectionsHits),
				getDurationToMonitor(),
				getTimeUnit(),
				getEarliestInstantAtSystemDefaultClockZoneId()
		);
	}

	@Override
	public void onNext(AccessLog accessLog) {
		if (accessLog != null) {
			updateTimeSlot();

			if (isTooOldForTimeSlot(accessLog.getInstant())) {
				statsOutputter.logOutsideDurationToMonitor(accessLog);
			} else {
				accessLogs.add(accessLog);
			}
		}
	}

	// I don't like doing two things at once (removing + computing the data) but it's more efficient
	private Map<String, Integer> computeSectionsHits() {
		updateTimeSlot();
		// Creating a new Map each time is slightly less efficient than having only one and calling .clear() on it, but it is more readable
		Map<String, Integer> sectionsHits = new HashMap<>();

		Iterator<AccessLog> accessLogsIterator = accessLogs.iterator();

		while (accessLogsIterator.hasNext()) {
			AccessLog accessLog = accessLogsIterator.next();

			if (isTooOldForTimeSlot(accessLog.getInstant())) {
				accessLogsIterator.remove();
			} else if (isNotTooRecentForTimeSlot(accessLog.getInstant())) {
				addSectionHitToMap(sectionsHits, accessLog);
			}
		}

		return sectionsHits;
	}

	private void addSectionHitToMap(Map<String, Integer> sectionsHits, AccessLog accessLog) {
		String section = accessLog.getSection();

		if (sectionsHits.containsKey(section)) {
			sectionsHits.put(section, sectionsHits.get(section) + 1);
		} else {
			sectionsHits.put(section, 1);
		}
	}

	private List<Map.Entry<String, Integer>> computeSectionsWithTheMostHits(Map<String, Integer> sectionsHits) {
		return sectionsHits.entrySet().stream()
				.sorted((entry1, entry2) ->
						Integer.compare(entry2.getValue(), entry1.getValue()) // This sort puts the biggest values first
				)
				.limit(numberOfSectionsToShow)
				.collect(Collectors.toList());
	}
}
