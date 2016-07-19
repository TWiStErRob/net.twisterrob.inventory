package net.twisterrob.android.content.glide;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.*;

public class WrappingTarget<Z> implements Target<Z> {
	@NonNull protected final Target<? super Z> target;
	public WrappingTarget(@NonNull Target<? super Z> target) {
		this.target = target;
	}

	@Override public void getSize(SizeReadyCallback cb) {
		target.getSize(cb);
	}

	@Override public void onLoadStarted(Drawable placeholder) {
		target.onLoadStarted(placeholder);
	}
	@Override public void onLoadFailed(Exception e, Drawable errorDrawable) {
		target.onLoadFailed(e, errorDrawable);
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
		target.onResourceReady(resource, (GlideAnimation)glideAnimation);
	}
	@Override public void onLoadCleared(Drawable placeholder) {
		target.onLoadCleared(placeholder);
	}

	@Override public Request getRequest() {
		return target.getRequest();
	}
	@Override public void setRequest(Request request) {
		target.setRequest(request);
	}

	@Override public void onStart() {
		target.onStart();
	}
	@Override public void onStop() {
		target.onStop();
	}
	@Override public void onDestroy() {
		target.onDestroy();
	}
	@Override public int hashCode() {
		return target.hashCode();
	}
	@Override public boolean equals(Object o) {
		if (o instanceof WrappingTarget) {
			return this.target.equals(((WrappingTarget<?>)o).target);
		}
		return super.equals(o);
	}
	@Override public String toString() {
		return "Wrapped " + target.toString();
	}
}
