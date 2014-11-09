package net.twisterrob.android.content.glide;

import org.slf4j.*;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class LoggingListener<T, Z> implements RequestListener<T, Z> {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingListener.class);
	private final String type;

	public LoggingListener(String type) {
		this.type = type;
	}

	public boolean onException(Exception e, T model, Target<Z> target, boolean isFirst) {
		LOG.warn("Cannot load {}/{} into {} (first={})", //
				type, model, target, isFirst, e);
		return false;
	}

	public boolean onResourceReady(Z resource, T model, Target<Z> target, boolean isFromMemCache, boolean isFirst) {
		LOG.debug("Loaded {}/{} into {} (first={}, mem={}) transcoded={}", //
				type, model, target, isFirst, isFromMemCache, resource);
		return false;
	}
}
