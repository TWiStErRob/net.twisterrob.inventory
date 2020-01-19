package net.twisterrob.android.content;

import java.io.File;
import java.util.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.Build.*;
import android.provider.MediaStore;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.android.capture_image.R;
import net.twisterrob.android.utils.tools.*;

// FIXME https://developer.android.com/guide/topics/providers/document-provider.html
public class ImageRequest {
	private final Activity activity;
	private final Intent intent;
	private final int requestCode;

	private ImageRequest(Intent intent, int requestCode, Activity activity) {
		this.intent = intent;
		this.requestCode = requestCode;
		this.activity = activity;
	}

	public Intent getIntent() {
		return intent;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public void start() {
		if (activity != null) {
			start(activity);
		} else {
			throw new IllegalStateException("Create the builder with an Activity to be able to use start(), "
					+ "or use start(Activity).");
		}
	}

	public Uri getPictureUriFromResult(int requestCode, int resultCode, Intent data) {
		Uri selectedImageUri = null;
		if (resultCode == Activity.RESULT_OK && requestCode == this.requestCode && data != null) {
			boolean isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
			if (isCamera) {
				selectedImageUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
			} else {
				selectedImageUri = data.getData();
			}
		}
		return selectedImageUri;
	}

	public void start(Activity activity) {
		activity.startActivityForResult(getIntent(), getRequestCode());
	}

	public static class Builder {
		private static final short REQUEST_CODE_BASE = 0x4100;
		private static final short REQUEST_CODE_PICK = 1 << 1;
		private static final short REQUEST_CODE_TAKE = 1 << 2;
		private static final short REQUEST_CODE_CROP = 1 << 3;
		private final Context context;
		private final List<Intent> intents = new ArrayList<>();
		private final Intent chooserIntent;
		private boolean requestCodeSet = false;
		private int requestCode = REQUEST_CODE_BASE;
		public Builder(Context context) {
			this.context = context;
			String title = context.getString(R.string.image__choose_external__title);
			this.chooserIntent = Intent.createChooser(new Intent(CaptureImage.ACTION), title);
		}

		public Builder withRequestCode(int requestCode) {
			this.requestCode = requestCode;
			this.requestCodeSet = true;
			return this;
		}
		public Builder addGalleryIntent() {
			if (!requestCodeSet) {
				requestCode |= REQUEST_CODE_PICK;
			}
			intents.addAll(AndroidTools.resolveIntents(context, createGalleryIntent(), 0));
			return this;
		}
		public Builder addCameraIntents(File file) {
			if (!requestCodeSet) {
				requestCode |= REQUEST_CODE_TAKE;
			}
			intents.addAll(createCameraIntents(context, Uri.fromFile(file)));
			return this;
		}
		public Builder addCameraIntents(Uri uri) {
			if (!requestCodeSet) {
				requestCode |= REQUEST_CODE_TAKE;
			}
			intents.addAll(createCameraIntents(context, uri));
			return this;
		}

		public ImageRequest build() {
			Intent[] intents = buildInitialIntents();
			Arrays.sort(intents, new IntentByLabelComparator(context.getPackageManager()));
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);

			Activity activity = context instanceof Activity? (Activity)context : null;
			return new ImageRequest(chooserIntent, requestCode, activity);
		}
		private Intent[] buildInitialIntents() {
			Intent[] intents = this.intents.toArray(new Intent[this.intents.size()]);
			PackageManager pm = context.getPackageManager();
			for (int i = 0; i < intents.length; i++) {
				Intent intent = intents[i];
				if (!(intent instanceof LabeledIntent)) {
					CharSequence appLabel = pm.resolveActivity(intent, 0).loadLabel(pm);
					CharSequence label;
					if (MediaStore.ACTION_IMAGE_CAPTURE.equals(intent.getAction())) {
						label = TextTools.formatFormatted(context,
								R.string.image__choose_external__intent_label_take, appLabel);
					} else if (Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
						label = TextTools.formatFormatted(context,
								R.string.image__choose_external__intent_label_pick, appLabel);
					} else {
						label = appLabel;
					}
					intent = new LabeledIntent(intent, null, label, 0);
				}
				intents[i] = intent;
			}
			return intents;
		}
	}

	public static void openImageInGallery(Activity activity, File sourceFile) {
		activity.startActivity(createOpenImageIntent(sourceFile));
	}

	private static Intent createOpenImageIntent(File sourceFile) {
		Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Uri uri = base.buildUpon().appendPath(sourceFile.getAbsolutePath()).build();
		return new Intent(Intent.ACTION_VIEW, uri);
	}

	private static List<Intent> createCameraIntents(Context context, Uri outputFileUri) {
		if (!canHasCamera(context)) {
			return Collections.emptyList();
		}
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		List<Intent> cameraIntents = AndroidTools.resolveIntents(context, captureIntent, 0);
		for (Intent intent : cameraIntents) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		}
		return cameraIntents;
	}

	private static Intent createGalleryIntent() {
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		return galleryIntent;
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
	@SuppressWarnings("deprecation")
	public static boolean canHasCamera(Context context) {
		PackageManager pm = context.getPackageManager();
		boolean hasCameraAny = VERSION_CODES.JELLY_BEAN_MR1 < VERSION.SDK_INT;
		return android.hardware.Camera.getNumberOfCameras() > 0 && (
				(hasCameraAny && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
						|| pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
						|| pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
		);
	}

	private static class IntentByLabelComparator implements Comparator<Intent> {
		private final PackageManager pm;
		public IntentByLabelComparator(PackageManager pm) {
			this.pm = pm;
		}
		@Override public int compare(Intent lhs, Intent rhs) {
			CharSequence lLabel = getLabel(lhs);
			CharSequence rLabel = getLabel(rhs);
			return lLabel.toString().compareTo(rLabel.toString());
		}
		private CharSequence getLabel(Intent intent) {
			if (intent instanceof LabeledIntent) {
				return ((LabeledIntent)intent).loadLabel(pm);
			} else {
				ResolveInfo info = pm.resolveActivity(intent, 0);
				return info.loadLabel(pm);
			}
		}
	}
}
