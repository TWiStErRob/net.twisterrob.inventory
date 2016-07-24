package net.twisterrob.inventory.android.activity;

import java.io.*;

import org.slf4j.*;

import android.content.*;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.stream.StreamUriLoader;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.AndroidTools.PopupCallbacks;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;

public class ImageActivity extends DebugHelperActivity implements RequestListener<Uri, Bitmap> {
	private static final Logger LOG = LoggerFactory.getLogger(ImageActivity.class);

	/** type: Boolean, true=internal, false=external, not present=auto(from prefs) */
	public static final String EXTRA_INTERNAL = "useInternal";

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image);
		ImageView image = (ImageView)findViewById(R.id.image);
		image.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				askRedirect();
			}
		});

		if (getExtraUseInternal()) {
			Glide
					.with(this)
					.using(new StreamUriLoader(getApplicationContext()))
					.load(getIntent().getData())
					.asBitmap()
					.format(DecodeFormat.PREFER_ARGB_8888)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.skipMemoryCache(true)
					.thumbnail(0.25f)
					.crossFade()
					.listener(this)
					.into(image)
			;
		} else {
			if (savedInstanceState == null) { // start only once
				redirect();
			}
		}
	}

	private void redirect() {
		new Redirect().execute(getIntent().getData());
	}

	private void askRedirect() {
		AndroidTools
				.confirm(this, new PopupCallbacks<Boolean>() {
					@Override public void finished(Boolean value) {
						if (Boolean.TRUE.equals(value)) {
							redirect();
						}
					}
				})
				.setTitle("Image viewer")
				.setMessage("The built-in image viewer has limited functionality. "
						+ "Would you like to open the image in another app?")
				.setNeutralButton("Always", new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						App.prefs().setBoolean(R.string.pref_internalImageViewer, false);
						redirect();
					}
				})
				.show()
		;
	}

	@Override
	public boolean onException(Exception ex, Uri model, Target<Bitmap> target, boolean isFirstResource) {
		LOG.warn("Cannot load image: {}", model, ex);
		App.toastUser(App.getError(ex, "Cannot load image."));
		finish();
		return true;
	}

	@Override
	public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target,
			boolean isFromMemoryCache, boolean isFirstResource) {
		return false;
	}

	private boolean getExtraUseInternal() {
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(EXTRA_INTERNAL)) {
			return extras.getBoolean(EXTRA_INTERNAL);
		}
		// if not overridden in extras, return the value in preferences
		return App.prefs().getBoolean(R.string.pref_internalImageViewer, R.bool.pref_internalImageViewer_default);
	}

	/** Optional {@link #EXTRA_INTERNAL} can be added. */
	public static Intent show(@NonNull Uri data) {
		Intent intent = new Intent(App.getAppContext(), ImageActivity.class);
		intent.setData(data);
		return intent;
	}

	/** External image viewers have horrible support for content:// uris so it's safer to hand them a temporary file. */
	private class Redirect extends SimpleSafeAsyncTask<Uri, Void, Uri> {
		@Override protected void onPreExecute() {
			findViewById(R.id.description).setVisibility(View.VISIBLE);
		}

		@Override protected Uri doInBackground(Uri uri) throws IOException {
			Context context = ImageActivity.this;
			File tempImage = Paths.getShareImage(context);
			IOTools.copyStream(context.getContentResolver().openInputStream(uri), new FileOutputStream(tempImage));
			return Paths.getShareUri(context, tempImage);
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
