package net.twisterrob.inventory.android.activity;

import java.io.*;

import org.slf4j.*;

import android.content.*;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;

@AndroidEntryPoint
public class ImageActivity extends DebugHelperActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ImageActivity.class);

	/** type: Boolean, true=internal, false=external, not present=auto(from prefs) */
	public static final String EXTRA_INTERNAL = "useInternal";

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image);
		ImageView image = findViewById(R.id.image);
		image.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				askRedirect();
			}
		});

		if (getExtraUseInternal()) {
			RequestBuilder<Bitmap> glide = Glide
					.with(this)
					.asBitmap()
					//STOPSHIP why? .using(new StreamUriLoader(getApplicationContext()))
					.load(getIntent().getData())
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					;
			glide
					.format(DecodeFormat.PREFER_ARGB_8888)
					.skipMemoryCache(true)
					.thumbnail(glide
							.clone()
							.format(DecodeFormat.PREFER_RGB_565)
							.sizeMultiplier(0.25f)
					)
					.transition(BitmapTransitionOptions.withCrossFade())
					.addListener(new GlideListener())
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
		DialogTools
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

	private void onError(@Nullable Exception ex, @Nullable Object model) {
		if (ex == null) {
			ex = new GlideException("Unknown error");
		}
		LOG.warn("Cannot load image: {}", model, ex);
		App.toastUser(App.getError(ex, "Cannot load image."));
		finish();
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
	
	private class GlideListener implements RequestListener<Bitmap> {

		@Override public boolean onLoadFailed(@Nullable GlideException ex, @Nullable Object model,
				@NonNull Target<Bitmap> target, boolean isFirstResource) {
			onError(ex, model);
			return true;
		}

		@Override public boolean onResourceReady(@NonNull Bitmap resource, @NonNull Object model,
				Target<Bitmap> target, @NonNull DataSource dataSource, boolean isFirstResource) {
			return false;
		}
	}

	/** External image viewers have horrible support for content:// uris so it's safer to hand them a temporary file. */
	private class Redirect extends SimpleSafeAsyncTask<Uri, Void, Uri> {
		@SuppressWarnings("deprecation")
		public void execute(@NonNull Uri node) {
			// Overridden to hide deprecation warnings at call-site.
			super.execute(node);
		}

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
			ImageActivity.this.onError(ex, param);
		}
	}
}
