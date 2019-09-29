package io.karon.logmonitor.inputter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.karon.logmonitor.configuration.Configuration;
import io.karon.logmonitor.log.Log;
import io.karon.logmonitor.parser.LogParser;
import io.karon.logmonitor.parser.ParseException;
import io.reactivex.Observer;
import io.reactivex.subjects.ReplaySubject;


/*
Monitors a log file and calls the `onNext(log)` of all the Observables subscribed when it reads a new log
 */
public class FileLogMonitor<T extends Log> implements LogMonitor<T> {
	private static final Logger logger = LogManager.getLogger(FileLogMonitor.class);

	private final File fileToMonitor;
	private final LogParser<T> logParser;
	private final boolean ignoreOnParseFailure;
	private final boolean readFromEndOfFile;

	private boolean monitor;
	private final ReplaySubject<T> replaySubject;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public FileLogMonitor(
			File fileToMonitor,
			LogParser<T> logParser,
			Configuration.FileLogMonitorConfiguration configuration) {
		this(fileToMonitor, logParser, configuration.isIgnoreOnParseFailure(), configuration.isReadFromEndOfFile());
	}

	public FileLogMonitor(
			File fileToMonitor,
			LogParser<T> logParser,
			boolean ignoreOnParseFailure,
			boolean readFromEndOfFile) {
		this.fileToMonitor = fileToMonitor;
		this.logParser = logParser;
		this.ignoreOnParseFailure = ignoreOnParseFailure;
		this.readFromEndOfFile = readFromEndOfFile;

		this.monitor = false;
		this.replaySubject = ReplaySubject.create();
	}

	@Override
	public void subscribe(Observer<T> observer) {
		replaySubject.subscribe(observer);
	}

	@Override
	public void startMonitoring() {
		monitor = true;

		// TODO: replace with flowable
		executorService.submit(() -> {
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToMonitor))) {
				logger.info("Monitoring log file: {}", fileToMonitor.getAbsolutePath());
				readFile(bufferedReader);
			} catch (InterruptedException | IOException e) {
				logger.error("Tasks interrupted", e);
				stopMonitoring();
			}
		});
	}

	void readFile(BufferedReader bufferedReader) throws IOException, InterruptedException {
		if (readFromEndOfFile) {
			long fileLength = fileToMonitor.length();
			bufferedReader.skip(fileLength);
		}

		while (monitor) {
			readLine(bufferedReader);
		}
	}

	void readLine(BufferedReader bufferedReader) throws IOException, InterruptedException {
		String line = bufferedReader.readLine();
		if (line == null || line.isEmpty()) {
			TimeUnit.MILLISECONDS.sleep(500);
		} else {
			try {
				replaySubject.onNext(logParser.parseFromLogLine(line));
			} catch (ParseException e) {
				if (ignoreOnParseFailure) {
					logger.debug(e);
				} else {
					// TODO: handle parse errors
					//  (not handled because in the context of this exercise I decided to ignore them, as it should not occur)
					logger.error(e);
				}
			}
		}
	}

	@Override
	public void stopMonitoring() {
		monitor = false;

		try {
			logger.info("Attempt to shutdown executor");
			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			// ignoring the exception because we are trying to interrupt the thread anyway
			logger.error("Tasks interrupted", e);
		}
		finally {
			if (!executorService.isTerminated()) {
				logger.error("Cancel non-finished tasks");
			}
			executorService.shutdownNow();
			logger.info("Shutdown finished");
		}
	}
}
