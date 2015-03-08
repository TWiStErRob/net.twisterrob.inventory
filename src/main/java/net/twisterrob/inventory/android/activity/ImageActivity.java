package net.twisterrob.inventory.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.twisterrob.inventory.android.*;

public class ImageActivity extends Activity implements RequestListener<Uri, GlideDrawable> {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image);
		ImageView image = (ImageView)findViewById(R.id.image);

		Glide
				.with(this)
				.load(getIntent().getData())
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.skipMemoryCache(true)
				.thumbnail(0.25f)
				.listener(this)
				.into(image)
		;
	}

	@Override
	public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
		finish();
		return true;
	}

	@Override
	public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target,
			boolean isFromMemoryCache, boolean isFirstResource) {
		return false;
	}

	public static Intent show(Uri data) {
		Intent intent = new Intent(App.getAppContext(), ImageActivity.class);
		intent.setData(data);
		return intent;
	}
}
