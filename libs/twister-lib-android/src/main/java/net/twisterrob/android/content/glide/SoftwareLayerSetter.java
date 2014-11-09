package net.twisterrob.android.content.glide;

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.*;

/**
 * Listener which updates the {@link ImageView} to be software rendered,
 * because {@link com.caverock.androidsvg.SVG SVG}/{@link android.graphics.Picture Picture}
 * can't render on a hardware backed {@link android.graphics.Canvas Canvas}.
 *
 * @param <T> not used, here to prevent unchecked warnings at usage
 * @param <R> not used, here to prevent unchecked warnings at usage
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SoftwareLayerSetter<T, R> implements RequestListener<T, R> {
	@Override
	public boolean onResourceReady(R resource, T model, Target<R> target, boolean isFromMemoryCache,
			boolean isFirstResource) {
		ImageView view = ((ImageViewTarget<?>)target).getView();
		if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
			view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
		}
		return false;
	}

	@Override
	public boolean onException(Exception e, T model, Target<R> target, boolean isFirstResource) {
		ImageView view = ((ImageViewTarget<?>)target).getView();
		if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
			view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
		}
		return false;
	}
}
