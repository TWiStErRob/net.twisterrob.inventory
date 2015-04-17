package net.twisterrob.android.content.glide;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class LoggingListener<T, Z> implements RequestListener<T, Z> {
	public interface ModelFormatter<T> {
		String toString(T model);
	}

	private static final Logger LOG = LoggerFactory.getLogger(LoggingListener.class);
	private final String type;
	private final ModelFormatter<? super T> formatter;

	public LoggingListener(String type) {
		this(type, STRING_FORMATTER);
	}

	public LoggingListener(String type, ModelFormatter<? super T> formatter) {
		this.type = type;
		this.formatter = formatter;
	}

	public boolean onException(Exception e, T model, Target<Z> target, boolean isFirst) {
		LOG.warn("Cannot load {}@{} into {} (first={})", type, formatter.toString(model), target, isFirst, e);
		return false;
	}

	public boolean onResourceReady(Z resource, T model, Target<Z> target, boolean isFromMemCache, boolean isFirst) {
		LOG.trace("Loaded {}@{} into {} (first={}, mem={}) transcoded={}", type, formatter.toString(model), target,
				isFirst, isFromMemCache, resource);
		return false;
	}

	private static final ModelFormatter<Object> STRING_FORMATTER = new ModelFormatter<Object>() {
		@Override public String toString(Object model) {
			return String.valueOf(model);
		}
	};

	public static class ResourceFormatter implements ModelFormatter<Integer> {
		private final Context context;

		public ResourceFormatter(Context context) {
			this.context = context;
		}

		@Override public String toString(Integer model) {
			try {
				return context.getResources().getResourceName(model).replace(context.getPackageName(), "app");
			} catch (NotFoundException ex) {
				return Integer.toHexString(model) + "=" + model;
			}
		}
	}
}
