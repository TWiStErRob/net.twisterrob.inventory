package net.twisterrob.inventory.android.utils.glide;

import android.view.View;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.drive.DriveId;

public class VisibilityToggler implements RequestListener<DriveId, GlideDrawable> {
	private final View view;

	public VisibilityToggler(View view) {
		this.view = view;
	}

	public boolean onResourceReady(GlideDrawable resource, DriveId model, Target<GlideDrawable> target,
			boolean isFromMemoryCache, boolean isFirstResource) {
		view.setVisibility(View.VISIBLE);
		return false;
	}

	public boolean onException(Exception e, DriveId model, Target<GlideDrawable> target, boolean isFirstResource) {
		view.setVisibility(View.INVISIBLE);
		return false;
	}
}