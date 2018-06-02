package net.twisterrob.android.content.glide;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.*;

/**
 * Listener which updates the {@link ImageView} to be software rendered,
 * because {@link com.caverock.androidsvg.SVG SVG}/{@link android.graphics.Picture Picture}
 * can't render on a hardware backed {@link android.graphics.Canvas Canvas}.
 *
 * @param <T> not used, exists to prevent unchecked warnings at usage
 * @param <R> not used, exists to prevent unchecked warnings at usage
 */
public class SoftwareLayerSetter<T, R> implements RequestListener<T, R> {
	@Override public boolean onResourceReady(R resource, T model, Target<R> target,
			boolean isFromMemoryCache, boolean isFirstResource) {
		View view = ((ViewTarget<?, R>)target).getView();
		ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_SOFTWARE, null);
		return false;
	}

	@Override public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
		View view = ((ViewTarget<?, R>)target).getView();
		ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_SOFTWARE, null);
		return false;
	}
}
