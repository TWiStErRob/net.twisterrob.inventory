package net.twisterrob.android.content.glide;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.*;

public class WrappingTarget<Z> implements Target<Z> {
	@NonNull protected final Target<? super Z> wrapped;
	public WrappingTarget(@NonNull Target<? super Z> wrapped) {
		this.wrapped = wrapped;
	}

	@Override public void getSize(SizeReadyCallback cb) {
		wrapped.getSize(cb);
	}

	@Override public void onLoadStarted(Drawable placeholder) {
		wrapped.onLoadStarted(placeholder);
	}
	@Override public void onLoadFailed(Exception e, Drawable errorDrawable) {
		wrapped.onLoadFailed(e, errorDrawable);
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
		wrapped.onResourceReady(resource, (GlideAnimation)glideAnimation);
	}
	@Override public void onLoadCleared(Drawable placeholder) {
		wrapped.onLoadCleared(placeholder);
	}

	@Override public Request getRequest() {
		return wrapped.getRequest();
	}
	@Override public void setRequest(Request request) {
		wrapped.setRequest(request);
	}

	@Override public void onStart() {
		wrapped.onStart();
	}
	@Override public void onStop() {
		wrapped.onStop();
	}
	@Override public void onDestroy() {
		wrapped.onDestroy();
	}
	@Override public int hashCode() {
		return wrapped.hashCode();
	}
	@Override public boolean equals(Object o) {
		if (o instanceof WrappingTarget) {
			return this.wrapped.equals(((WrappingTarget<?>)o).wrapped);
		}
		return super.equals(o);
	}
	@Override public String toString() {
		return "Wrapped " + wrapped.toString();
	}
}
