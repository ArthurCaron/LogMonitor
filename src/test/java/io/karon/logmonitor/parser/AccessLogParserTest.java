package io.karon.logmonitor.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.karon.logmonitor.log.AccessLog;
import io.karon.logmonitor.LogGenerator;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccessLogParserTest {
	private AccessLogParser accessLogParser;

	@BeforeAll
	void beforeAll() {
		accessLogParser = new AccessLogParser();
	}

	@Test
	final void testToString() {
		AccessLog accessLog = LogGenerator.generateLog();
		String expectedResult = accessLog.getIpAddress() + " "
				+ accessLog.getClientIdentd() + " "
				+ accessLog.getUserID() + " "
				+ "[" + AccessLog.DATE_TIME_FORMATTER.format(accessLog.getZonedDateTime()) + "] "
				+ "\"" + accessLog.getMethod() + " " + accessLog.getEndpoint() + " " + accessLog.getProtocol() + "\" "
				+ accessLog.getResponseCode() + " "
				+ accessLog.getContentSize();

		assertEquals(expectedResult, accessLog.toString());
	}

	@Test
	final void testParseFromLogLine() throws ParseException {
		AccessLog accessLog = LogGenerator.generateLog();
		AccessLog parsedResult = accessLogParser.parseFromLogLine(accessLog.toString());

		assertNotNull(parsedResult);
		assertEquals(accessLog.toString(), parsedResult.toString());
	}

	@Test
	final void testParseFromNullLogLine() {
		assertThrows(ParseException.class, () -> accessLogParser.parseFromLogLine(null));
	}

	@Test
	final void testParseFromBadLogLine() {
		assertThrows(ParseException.class, () -> accessLogParser.parseFromLogLine("BAD LOG LINE"));
	}
}
