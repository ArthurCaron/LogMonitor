package io.karon.logmonitor.listener;

import java.util.concurrent.TimeUnit;

import io.karon.logmonitor.log.AccessLog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public abstract class ScheduledLogListener implements Observer<AccessLog> {
	private Disposable disposable;

	private final long initialDelay;
	private final long period;
	private final TimeUnit timeUnit;

	ScheduledLogListener(long initialDelay, long period, TimeUnit timeUnit) {
		this.initialDelay = initialDelay;
		this.period = period;
		this.timeUnit = timeUnit;
	}

	abstract void runScheduledProcess();

	public void startScheduler() {
		startScheduler(Schedulers.computation());
	}

	public void startScheduler(Scheduler scheduler) {
		disposable = Observable.interval(initialDelay, period, timeUnit)
				.subscribeOn(scheduler)
				.subscribe(ignored -> runScheduledProcess());
	}

	public void stopScheduler() {
		disposable.dispose();
	}

	@Override
	public void onSubscribe(Disposable d) {
		// Do nothing
	}

	@Override
	public void onError(Throwable e) {
		// TODO: handle error -> we should probably try to restart everything when it crashes because of I/O errors
		stopScheduler();
	}

	@Override
	public void onComplete() {
		stopScheduler();
	}

	TimeUnit getTimeUnit() { return timeUnit; }
}
