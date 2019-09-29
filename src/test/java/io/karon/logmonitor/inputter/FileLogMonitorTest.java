package io.karon.logmonitor.inputter;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.parser.AccessLogParser;
import io.karon.logmonitor.parser.ParseException;
import io.karon.logmonitor.LogGenerator;
import io.reactivex.Observer;


/*
There are no tests for actually monitoring a file.
I could make one, but I would need to modify drastically FileLogMonitor to have a better access to the different Readers, in order to Mock them.
I decided against it.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileLogMonitorTest {
	private BufferedReader bufferedReader;
	private File fileToMonitor;
	private AccessLogParser accessLogParser;
	private FileLogMonitor<AccessLog> fileLogMonitor;

	@Mock // I try to avoid using annotations when possible, but this one avoids a warning due to the generic parameter
	private Observer<AccessLog> observer;

	@BeforeAll
	void beforeAll() {
		MockitoAnnotations.initMocks(this);
		fileToMonitor = mock(File.class);
		bufferedReader = mock(BufferedReader.class);
		accessLogParser = mock(AccessLogParser.class);
		fileLogMonitor = new FileLogMonitor<>(fileToMonitor, accessLogParser, true, true);
		fileLogMonitor.subscribe(observer);
	}

	@Test
	void ignoreBeginningOfFileTest() throws IOException, InterruptedException {
		doReturn(100L).when(fileToMonitor).length();

		fileLogMonitor.readFile(bufferedReader);

		verify(bufferedReader).skip(100L);
	}

	@Test
	void readLineIsSentToSubjectSubscriberTest() throws IOException, InterruptedException, ParseException {
		String logLine = "logLine";
		doReturn(logLine).when(bufferedReader).readLine();

		AccessLog accessLog = LogGenerator.generateLog();
		doReturn(accessLog).when(accessLogParser).parseFromLogLine(logLine);

		fileLogMonitor.readLine(bufferedReader);

		verify(observer).onNext(accessLog);
	}
}
