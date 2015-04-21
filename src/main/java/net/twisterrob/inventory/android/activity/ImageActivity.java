package net.twisterrob.inventory.android.activity;

import java.io.*;

import org.slf4j.*;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.stream.StreamUriLoader;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;

public class ImageActivity extends VariantActivity implements RequestListener<Uri, GlideDrawable> {
	private static final Logger LOG = LoggerFactory.getLogger(ImageActivity.class);

	/** type: Boolean, true=internal, false=external, not present=auto(from prefs) */
	public static final String EXTRA_INTERNAL = "useInternal";

	private ImageView image;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image);
		image = (ImageView)findViewById(R.id.image);

		if (getExtraUseInternal()) {
			Glide
					.with(this)
					.using(new StreamUriLoader(this))
					.load(getIntent().getData())
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.skipMemoryCache(true)
					.dontAnimate()
					.thumbnail(0.25f)
					.listener(this)
					.into(image)
			;
		} else {
			if (savedInstanceState == null) { // start only once
				new Redirect().execute(getIntent().getData());
			}
		}
	}

	@Override
	public boolean onException(Exception ex, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
		App.toastUser("Cannot load image.");
		LOG.warn("Cannot load image: {}", model, ex);
		finish();
		return true;
	}

	@Override
	public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target,
			boolean isFromMemoryCache, boolean isFirstResource) {
		return false;
	}

	private boolean getExtraUseInternal() {
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(EXTRA_INTERNAL)) {
			return extras.getBoolean(EXTRA_INTERNAL);
		}
		// if not overridden in extras, return the value in preferences
		return App.getPrefs().getBoolean(
				getResources().getString(R.string.pref_internalImageViewer),
				getResources().getBoolean(R.bool.pref_internalImageViewer_default));
	}

	/** Optional {@link #EXTRA_INTERNAL} can be added. */
	public static Intent show(@NonNull Uri data) {
		Intent intent = new Intent(App.getAppContext(), ImageActivity.class);
		intent.setData(data);
		return intent;
	}

	private class Redirect extends SimpleSafeAsyncTask<Uri, Void, Uri> {
		@Override protected void onPreExecute() {
			image.setImageResource(R.drawable.image_loading);
		}

		@Override protected Uri doInBackground(Uri uri) throws IOException {
			Context context = ImageActivity.this;
			File tempImage = Paths.getShareImage(context);
			IOTools.copyStream(context.getContentResolver().openInputStream(uri), new FileOutputStream(tempImage));
			String authority = AndroidTools.findProviderAuthority(context, FileProvider.class).authority;
			return FileProvider.getUriForFile(context, authority, tempImage);
		}

		@Override protected void onResult(Uri result, Uri param) {
			Intent intent = new Intent(Intent.ACTION_VIEW)
					.setDataAndType(result, "image/jpeg")
					.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			//grantUriPermission("com.sec.android.gallery3d", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
			//grantUriPermission("com.alensw.PicFolder", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(intent);
			finish();
		}

		@Override protected void onError(@NonNull Exception ex, Uri param) {
			onException(ex, param, null, true);
		}
	}
}
