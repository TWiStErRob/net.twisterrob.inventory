package net.twisterrob.android.view;

import org.slf4j.*;

import android.os.Handler;
import android.widget.ViewSwitcher;

public class AutomatedViewSwitcher {
	private static final Logger LOG = LoggerFactory.getLogger(AutomatedViewSwitcher.class);

	private final ViewSwitcher switcher;
	private final long[] delays;
	private final Handler handler;

	public AutomatedViewSwitcher(ViewSwitcher switcher, long firstDelay, long secondDelay) {
		this.switcher = switcher;
		if (switcher.getChildCount() != 2) {
			throw new IllegalArgumentException("Need a switcher that has two children.");
		}
		firstDelay += switcher.getOutAnimation().getDuration() + switcher.getInAnimation().getDuration();
		secondDelay += switcher.getOutAnimation().getDuration() + switcher.getInAnimation().getDuration();
		this.delays = new long[] {firstDelay, secondDelay};
		this.handler = new Handler();
	}
	public void start() {
		int current = switcher.getDisplayedChild();
		long delay = delays[current];
//		LOG.trace("Switching from child #{} after {} milliseconds", current, delay);
		handler.postDelayed(next, delay);
	}
	public void stop() {
		handler.removeCallbacks(next);
	}

	private final Runnable next = new Runnable() {
		@Override public void run() {
			switcher.setDisplayedChild((switcher.getDisplayedChild() + 1) % switcher.getChildCount());
			start();
		}
	};
}
