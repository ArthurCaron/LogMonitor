package io.karon.logmonitor.configuration;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Configuration {
	private static final Logger logger = LogManager.getLogger(Configuration.class);
	private static final String CONFIGURATION_FILE_NAME = "configuration.properties";

	private FileToMonitorConfiguration fileToMonitorConfiguration;
	private FileLogMonitorConfiguration fileLogMonitorConfiguration;
	private StatsLogListenerConfiguration statsLogListenerConfiguration;
	private ThresholdLogListenerConfiguration thresholdLogListenerConfiguration;

	public static Configuration getConfigValues() {
		Configuration configuration = new Configuration();

		Properties properties = new Properties();

		try (InputStream inputStream = configuration.getClass().getClassLoader().getResourceAsStream(CONFIGURATION_FILE_NAME)) {

			if (inputStream != null) {
				properties.load(inputStream);

				configuration.fileToMonitorConfiguration = FileToMonitorConfiguration.getConfiguration(properties);
				configuration.fileLogMonitorConfiguration = FileLogMonitorConfiguration.getConfiguration(properties);
				configuration.statsLogListenerConfiguration = StatsLogListenerConfiguration.getConfiguration(properties);
				configuration.thresholdLogListenerConfiguration = ThresholdLogListenerConfiguration.getConfiguration(properties);
			} else {
				logger.info("Configuration file not found, revert to using default properties");

				configuration.fileToMonitorConfiguration = FileToMonitorConfiguration.getDefaultConfiguration();
				configuration.fileLogMonitorConfiguration = FileLogMonitorConfiguration.getDefaultConfiguration();
				configuration.statsLogListenerConfiguration = StatsLogListenerConfiguration.getDefaultConfiguration();
				configuration.thresholdLogListenerConfiguration = ThresholdLogListenerConfiguration.getDefaultConfiguration();
			}

		} catch (Exception e) {
			logger.warn("Exception occurred while trying to read the configuration file", e);
		}

		return configuration;
	}

	public FileToMonitorConfiguration getFileToMonitorConfiguration() {
		return fileToMonitorConfiguration;
	}

	public FileLogMonitorConfiguration getFileLogMonitorConfiguration() { return fileLogMonitorConfiguration; }

	public StatsLogListenerConfiguration getStatsLogListenerConfiguration() { return statsLogListenerConfiguration; }

	public ThresholdLogListenerConfiguration getThresholdLogListenerConfiguration() { return thresholdLogListenerConfiguration; }

	public static class FileToMonitorConfiguration {
		private static final String PATH_PROPERTY = "fileToMonitor.path";
		private static final String CREATE_IF_ABSENT_PROPERTY = "fileToMonitor.createIfAbsent";

		private static final String PATH_DEFAULT_VALUE = "/tmp/access.log";
		private static final String CREATE_IF_ABSENT_DEFAULT_VALUE = "true";

		private final File fileToMonitor;
		private final boolean createIfAbsent;

		private FileToMonitorConfiguration(String fileToMonitor, String createIfAbsent) {
			this.fileToMonitor = new File(fileToMonitor);
			this.createIfAbsent = Boolean.parseBoolean(createIfAbsent);
		}

		static FileToMonitorConfiguration getConfiguration(Properties properties) {
			return new FileToMonitorConfiguration(
					properties.getProperty(PATH_PROPERTY, PATH_DEFAULT_VALUE),
					properties.getProperty(CREATE_IF_ABSENT_PROPERTY, CREATE_IF_ABSENT_DEFAULT_VALUE)
			);
		}

		static FileToMonitorConfiguration getDefaultConfiguration() {
			return new FileToMonitorConfiguration(PATH_DEFAULT_VALUE, CREATE_IF_ABSENT_DEFAULT_VALUE);
		}

		public File getFileToMonitor() {
			return fileToMonitor;
		}

		public boolean isCreateIfAbsent() {
			return createIfAbsent;
		}
	}

	public static class FileLogMonitorConfiguration {
		private static final String IGNORE_ON_PARSE_FAILURE_PROPERTY = "fileLogMonitor.ignoreOnParseFailure";
		private static final String READ_FROM_END_OF_FILE_PROPERTY = "fileLogMonitor.readFromEndOfFile";

		private static final String IGNORE_ON_PARSE_FAILURE_DEFAULT_VALUE = "true";
		private static final String READ_FROM_END_OF_FILE_DEFAULT_VALUE = "true";

		private final boolean ignoreOnParseFailure;
		private final boolean readFromEndOfFile;

		private FileLogMonitorConfiguration(String ignoreOnParseFailure, String readFromEndOfFile) {
			this.ignoreOnParseFailure = Boolean.parseBoolean(ignoreOnParseFailure);
			this.readFromEndOfFile = Boolean.parseBoolean(readFromEndOfFile);
		}

		static FileLogMonitorConfiguration getConfiguration(Properties properties) {
			return new FileLogMonitorConfiguration(
					properties.getProperty(IGNORE_ON_PARSE_FAILURE_PROPERTY, IGNORE_ON_PARSE_FAILURE_DEFAULT_VALUE),
					properties.getProperty(READ_FROM_END_OF_FILE_PROPERTY, READ_FROM_END_OF_FILE_DEFAULT_VALUE)
			);
		}

		static FileLogMonitorConfiguration getDefaultConfiguration() {
			return new FileLogMonitorConfiguration(
					IGNORE_ON_PARSE_FAILURE_DEFAULT_VALUE,
					READ_FROM_END_OF_FILE_DEFAULT_VALUE
			);
		}

		public boolean isIgnoreOnParseFailure() { return ignoreOnParseFailure; }

		public boolean isReadFromEndOfFile() { return readFromEndOfFile; }
	}

	public static class StatsLogListenerConfiguration {
		private static final String INITIAL_DELAY_PROPERTY = "statsLogListener.initialDelay";
		private static final String PERIOD_PROPERTY = "statsLogListener.period";
		private static final String TIME_UNIT_PROPERTY = "statsLogListener.timeUnit";
		private static final String NUMBER_OF_SECTIONS_TO_SHOW_PROPERTY = "statsLogListener.numberOfSectionsToShow";

		private static final String INITIAL_DELAY_DEFAULT_VALUE = "10";
		private static final String PERIOD_DEFAULT_VALUE = "10";
		private static final String TIME_UNIT_DEFAULT_VALUE = "SECONDS";
		private static final String NUMBER_OF_SECTIONS_TO_SHOW_DEFAULT_VALUE = "5";

		private final int initialDelay;
		private final int period;
		private final TimeUnit timeUnit;
		private final int numberOfSectionsToShow;

		private StatsLogListenerConfiguration(
				String initialDelay,
				String period,
				String timeUnit,
				String numberOfSectionsToShow) {
			this.initialDelay = Integer.parseInt(initialDelay);
			this.period = Integer.parseInt(period);
			this.timeUnit = TimeUnit.valueOf(timeUnit);
			this.numberOfSectionsToShow = Integer.parseInt(numberOfSectionsToShow);
		}

		static StatsLogListenerConfiguration getConfiguration(Properties properties) {
			return new StatsLogListenerConfiguration(
					properties.getProperty(INITIAL_DELAY_PROPERTY, INITIAL_DELAY_DEFAULT_VALUE),
					properties.getProperty(PERIOD_PROPERTY, PERIOD_DEFAULT_VALUE),
					properties.getProperty(TIME_UNIT_PROPERTY, TIME_UNIT_DEFAULT_VALUE),
					properties.getProperty(NUMBER_OF_SECTIONS_TO_SHOW_PROPERTY, NUMBER_OF_SECTIONS_TO_SHOW_DEFAULT_VALUE)
			);
		}

		static StatsLogListenerConfiguration getDefaultConfiguration() {
			return new StatsLogListenerConfiguration(
					INITIAL_DELAY_DEFAULT_VALUE,
					PERIOD_DEFAULT_VALUE,
					TIME_UNIT_DEFAULT_VALUE,
					NUMBER_OF_SECTIONS_TO_SHOW_DEFAULT_VALUE
			);
		}

		public int getInitialDelay() { return initialDelay; }

		public int getPeriod() { return period; }

		public TimeUnit getTimeUnit() { return timeUnit; }

		public int getNumberOfSectionsToShow() { return numberOfSectionsToShow; }
	}

	public static class ThresholdLogListenerConfiguration {
		private static final String INITIAL_DELAY_PROPERTY = "thresholdLogListener.initialDelay";
		private static final String PERIOD_PROPERTY = "thresholdLogListener.period";
		private static final String DURATION_TO_MONITOR_PROPERTY = "thresholdLogListener.durationToMonitor";
		private static final String THRESHOLD_PROPERTY = "thresholdLogListener.threshold";
		private static final String TIME_UNIT_PROPERTY = "thresholdLogListener.timeUnit";

		private static final String INITIAL_DELAY_DEFAULT_VALUE = "10";
		private static final String PERIOD_DEFAULT_VALUE = "10";
		private static final String DURATION_TO_MONITOR_DEFAULT_VALUE = "120";
		private static final String THRESHOLD_DEFAULT_VALUE = "10";
		private static final String TIME_UNIT_DEFAULT_VALUE = "SECONDS";

		private final int initialDelay;
		private final int period;
		private final int durationToMonitor;
		private final int threshold;
		private final TimeUnit timeUnit;

		private ThresholdLogListenerConfiguration(
				String initialDelay,
				String period,
				String durationToMonitor,
				String threshold,
				String timeUnit) {
			this.initialDelay = Integer.parseInt(initialDelay);
			this.period = Integer.parseInt(period);
			this.durationToMonitor = Integer.parseInt(durationToMonitor);
			this.threshold = Integer.parseInt(threshold);
			this.timeUnit = TimeUnit.valueOf(timeUnit);
		}

		static ThresholdLogListenerConfiguration getConfiguration(Properties properties) {
			return new ThresholdLogListenerConfiguration(
					properties.getProperty(INITIAL_DELAY_PROPERTY, INITIAL_DELAY_DEFAULT_VALUE),
					properties.getProperty(PERIOD_PROPERTY, PERIOD_DEFAULT_VALUE),
					properties.getProperty(DURATION_TO_MONITOR_PROPERTY, DURATION_TO_MONITOR_DEFAULT_VALUE),
					properties.getProperty(THRESHOLD_PROPERTY, THRESHOLD_DEFAULT_VALUE),
					properties.getProperty(TIME_UNIT_PROPERTY, TIME_UNIT_DEFAULT_VALUE)
			);
		}

		static ThresholdLogListenerConfiguration getDefaultConfiguration() {
			return new ThresholdLogListenerConfiguration(
					INITIAL_DELAY_DEFAULT_VALUE,
					PERIOD_DEFAULT_VALUE,
					DURATION_TO_MONITOR_DEFAULT_VALUE,
					THRESHOLD_DEFAULT_VALUE,
					TIME_UNIT_DEFAULT_VALUE
			);
		}

		public int getInitialDelay() { return initialDelay; }

		public int getPeriod() { return period; }

		public int getDurationToMonitor() { return durationToMonitor; }

		public int getThreshold() { return threshold; }

		public TimeUnit getTimeUnit() { return timeUnit; }
	}
}
