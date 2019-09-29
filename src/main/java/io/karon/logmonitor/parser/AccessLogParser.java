package io.karon.logmonitor.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.karon.logmonitor.log.AccessLog;


/**
 * I found the original code here:
 * https://github.com/databricks/reference-apps/blob/master/logs_analyzer/chapter1/java8/src/main/java/com/databricks/apps/logs/ApacheAccessLog.java
 *
 * This class represents an Apache access log line.
 * See http://httpd.apache.org/docs/2.2/logs.html for more details.
 */
public class AccessLogParser implements LogParser<AccessLog> {
	private static final Logger logger = LogManager.getLogger(AccessLogParser.class);

	private static final String STANDARD_STRING_REGEX = "(\\S+)";
	private static final String DATE_REGEX = "([\\w:/]+\\s[+\\-]\\d{4})";
	private static final String RESPONSE_CODE_REGEX = "(\\d{3})";
	private static final String CONTENT_SIZE_REGEX = "(\\d+)";

	// Example NCSA log line:
	// |> 127.0.0.1 - james [09/May/2018:16:00:39 +0000] "GET /report HTTP/1.0" 200 123 <|
	private static final String NCSA_COMMON_LOG_FORMAT = "^"
			+ STANDARD_STRING_REGEX + " " // ipAddress
			+ STANDARD_STRING_REGEX + " " // clientIdentd
			+ STANDARD_STRING_REGEX + " " // userID
			+ "\\[" + DATE_REGEX + "] "   // zonedDateTime
			+ "\""
			+ STANDARD_STRING_REGEX + " " // method
			+ STANDARD_STRING_REGEX + " " // endpoint
			+ STANDARD_STRING_REGEX 	  // protocol
			+ "\" "
			+ RESPONSE_CODE_REGEX + " "   // responseCode
			+ CONTENT_SIZE_REGEX; 		  // contentSize

	private static final Pattern PATTERN = Pattern.compile(NCSA_COMMON_LOG_FORMAT);

	@Override
	public AccessLog parseFromLogLine(String logLine) throws ParseException {
		if (logLine == null) {
			throw new ParseException("The log line must not be null");
		}

		Matcher matcher = PATTERN.matcher(logLine);

		if (!matcher.find()) {
			logger.error("Cannot parse log line: |> {} <|", logLine);
			throw new ParseException("The log line |> " + logLine + " <| does not match the NCSA common log format");
		}

		return new AccessLog(
				matcher.group(1),
				matcher.group(2),
				matcher.group(3),
				matcher.group(4),
				matcher.group(5),
				matcher.group(6),
				matcher.group(7),
				matcher.group(8),
				matcher.group(9)
		);
	}
}
