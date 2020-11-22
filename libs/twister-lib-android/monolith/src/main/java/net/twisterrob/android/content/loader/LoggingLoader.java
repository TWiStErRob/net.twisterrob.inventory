package net.twisterrob.android.content.loader;

import org.slf4j.*;

import android.content.Context;

import androidx.loader.content.Loader;

import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class LoggingLoader<T> extends Loader<T> {
	@SuppressWarnings("CanBeFinal") // allow children to override
	protected Logger LOG = LoggerFactory.getLogger(getClass());

	public LoggingLoader(Context context) {
		super(context);
	}

	@Override protected void onStartLoading() {
		LOG.debug("{}.onStartLoading()", this);
		super.onStartLoading();
	}

	@Override protected void onStopLoading() {
		LOG.debug("{}.onStopLoading()", this);
		super.onStopLoading();
	}

	@Override protected void onForceLoad() {
		LOG.debug("{}.onForceLoad()", this);
		super.onForceLoad();
	}

	@Override protected void onReset() {
		LOG.debug("{}.onReset()", this);
		super.onReset();
	}

	@Override protected void onAbandon() {
		LOG.debug("{}.onAbandon()", this);
		super.onAbandon();
	}

	@Override public void deliverResult(T data) {
		LOG.debug("{}.deliverResult({})", this, data);
		super.deliverResult(data);
	}
}
