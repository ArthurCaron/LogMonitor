package io.karon.logmonitor;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.karon.logmonitor.configuration.Configuration;
import io.karon.logmonitor.inputter.FileLogMonitor;
import io.karon.logmonitor.listener.ScheduledLogListener;
import io.karon.logmonitor.listener.StatsLogListener;
import io.karon.logmonitor.listener.ThresholdLogListener;
import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.outputter.StatsToConsoleOutputter;
import io.karon.logmonitor.outputter.ThresholdToConsoleOutputter;
import io.karon.logmonitor.parser.AccessLogParser;


/*
Simple console program that monitors HTTP traffic on your machine.
Consume an actively written-to w3c-formatted HTTP access log (https://www.w3.org/Daemon/User/Config/Logging.html).
 */

public class Main {
	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		Configuration configuration = Configuration.getConfigValues();

		File fileToMonitor = getFileToMonitor(
				configuration.getFileToMonitorConfiguration(),
				args
		);

		FileLogMonitor<AccessLog> fileLogMonitor = new FileLogMonitor<>(
				fileToMonitor,
				new AccessLogParser(),
				configuration.getFileLogMonitorConfiguration()
		);

		ScheduledLogListener thresholdLogListener = new ThresholdLogListener(
				new ThresholdToConsoleOutputter(LogManager.getLogger(ThresholdToConsoleOutputter.class)),
				configuration.getThresholdLogListenerConfiguration()
		);
		ScheduledLogListener statsLogListener = new StatsLogListener(
				new StatsToConsoleOutputter(LogManager.getLogger(StatsToConsoleOutputter.class)),
				configuration.getStatsLogListenerConfiguration()
		);

		thresholdLogListener.startScheduler();
		statsLogListener.startScheduler();

		fileLogMonitor.subscribe(thresholdLogListener);
		fileLogMonitor.subscribe(statsLogListener);

		fileLogMonitor.startMonitoring();

		// Since I'm using Observables, there's nothing blocking the main thread, which is why I've added an infinite loop
		while (true) {}
	}

	private static File getFileToMonitor(
			Configuration.FileToMonitorConfiguration configuration,
			String[] args) throws IOException {
		File fileToMonitor;

		// If the path is given by argument, it overrides the configuration
		if (args.length >= 1 && args[0] != null && !args[0].isEmpty()) {
			fileToMonitor = new File(args[0]);
		} else {
			fileToMonitor = configuration.getFileToMonitor();
		}

		if (!fileToMonitor.exists()) {
			if (configuration.isCreateIfAbsent()) {
				createFileRecursively(fileToMonitor);
			} else {
				throw new IllegalArgumentException("The log file doesn't exists, and the property \"createIfAbsent\" is false");
			}
		}

		if (!fileToMonitor.isFile()) {
			throw new IllegalArgumentException("The log file path provided points to something that is not a file");
		}

		return fileToMonitor;
	}

	private static void createFileRecursively(File file) throws IOException {
		logger.debug("Specified log file does not exists. We will try creating one");
		boolean success = file.getParentFile().exists() || file.getParentFile().mkdirs();
		if (!success) {
			throw new IOException("Couldn't create parent folder of log file path");
		} else {
			success = file.createNewFile();
			if (!success) {
				throw new IOException("Couldn't create log file in provided path");
			} else {
				logger.debug("Log file successfully created");
			}
		}
	}
}
