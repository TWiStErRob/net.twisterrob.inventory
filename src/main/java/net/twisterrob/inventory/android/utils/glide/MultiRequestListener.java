package net.twisterrob.inventory.android.utils.glide;

import java.util.*;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class MultiRequestListener<T, R> implements RequestListener<T, R> {
	private final Collection<? extends RequestListener<T, R>> listeners;

	@SafeVarargs
	public MultiRequestListener(RequestListener<T, R>... listeners) {
		this(Arrays.asList(listeners));
	}

	public MultiRequestListener(Collection<? extends RequestListener<T, R>> listeners) {
		this.listeners = listeners;
	}

	public boolean onResourceReady(R resource, T model, Target<R> target, boolean isFromMemCache, boolean isFirst) {
		for (RequestListener<T, R> listener: listeners) {
			if (listener != null && listener.onResourceReady(resource, model, target, isFromMemCache, isFirst)) {
				return true;
			}
		}
		return false;
	}

	public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
		for (RequestListener<T, R> listener: listeners) {
			if (listener != null && listener.onException(e, model, target, isFirstResource)) {
				return true;
			}
		}
		return false;
	}
}
