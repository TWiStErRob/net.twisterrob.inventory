package net.twisterrob.android.utils.listeners;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.view.*;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressLint("ClickableViewAccessibility")
@DebugHelper
public class LoggingOnTouchListener implements View.OnTouchListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("ViewOnTouch");
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
