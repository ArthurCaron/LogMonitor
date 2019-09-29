package io.karon.logmonitor.parser;

import io.karon.logmonitor.log.Log;


public interface LogParser<T extends Log> {
	T parseFromLogLine(String logLine) throws ParseException;
}
