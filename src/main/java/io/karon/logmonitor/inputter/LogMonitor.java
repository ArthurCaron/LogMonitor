package io.karon.logmonitor.inputter;

import io.karon.logmonitor.log.Log;
import io.reactivex.Observer;


public interface LogMonitor<T extends Log> {
	void subscribe(Observer<T> observer);
	void startMonitoring();
	void stopMonitoring();
}
