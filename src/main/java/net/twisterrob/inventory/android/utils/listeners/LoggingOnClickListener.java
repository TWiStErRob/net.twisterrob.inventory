package net.twisterrob.inventory.android.utils.listeners;

import org.slf4j.*;

import android.view.View;

public class LoggingOnClickListener implements View.OnClickListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("ViewOnClick");
	private final Logger LOG;

	public LoggingOnClickListener() {
		this(LOGGER);
	}

	public LoggingOnClickListener(Logger log) {
		LOG = log;
	}

	@Override public void onClick(View v) {
		LOG.trace("onClick({})", v);
	}
}
