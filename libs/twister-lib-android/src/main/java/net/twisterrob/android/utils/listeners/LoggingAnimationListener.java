package net.twisterrob.android.utils.listeners;

import org.slf4j.*;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class LoggingAnimationListener implements AnimationListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("Animation");
	private final Logger LOG;

	public LoggingAnimationListener() {
		this(LOGGER);
	}

	public LoggingAnimationListener(Logger log) {
		LOG = log;
	}

	@Override public void onAnimationStart(Animation animation) {
		LOG.trace("onAnimationStart({})", animation);
	}
	@Override public void onAnimationEnd(Animation animation) {
		LOG.trace("onAnimationEnd({})", animation);
	}
	@Override public void onAnimationRepeat(Animation animation) {
		LOG.trace("onAnimationRepeat({})", animation);
	}
}
