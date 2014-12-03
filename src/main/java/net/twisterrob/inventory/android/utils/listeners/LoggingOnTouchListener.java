package net.twisterrob.inventory.android.utils.listeners;

import org.slf4j.*;

import android.view.*;

public class LoggingOnTouchListener implements View.OnTouchListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("RecyclerViewOnScroll");
	private final Logger LOG;

	public LoggingOnTouchListener() {
		this(LOGGER);
	}

	public LoggingOnTouchListener(Logger log) {
		LOG = log;
	}

	@Override public boolean onTouch(View v, MotionEvent event) {
		LOG.trace("onTouch({}, {})", v, event);
		return false;
	}
}
