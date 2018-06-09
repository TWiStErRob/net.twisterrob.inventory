package net.twisterrob.android.activity;

import java.io.*;

import org.slf4j.*;

import android.Manifest;
import android.animation.*;
import android.annotation.*;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.Build.*;
import android.os.*;
import android.provider.MediaStore;
import android.support.annotation.*;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bumptech.glide.*;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.ImageVideoWrapper;
import com.bumptech.glide.load.resource.bitmap.*;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.*;
import com.bumptech.glide.request.target.*;

import net.twisterrob.android.capture_image.R;
import net.twisterrob.android.content.ImageRequest;
import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.utils.concurrent.Callback;
import net.twisterrob.android.utils.tools.ImageTools;
import net.twisterrob.android.view.*;
import net.twisterrob.android.view.CameraPreview.*;
import net.twisterrob.android.view.SelectionView.SelectionStatus;
import net.twisterrob.java.io.IOTools;

/**
 * TODO check how others did it
 * <a href="https://github.com/lvillani/android-cropimage/tree/678f453d577232bbeed6b025dace823fa6bee43b">Crop Image from Gallery (as was 2014)</a>
 * <br>
 * <a href="http://adblogcat.com/a-camera-preview-with-a-bounding-box-like-google-goggles/">A camera preview with a bounding box like Google goggles</a>
 * > <a href="http://mobile.mymasterpeice.comxa.com/wp-content/uploads/2015/10/adblogcat.com-A-camera-preview-with-a-bounding-box-like-Google-goggles.pdf">as PDF</a>
 * > <a href="https://code.google.com/archive/p/ece301-examples/downloads">Downloads</a>
 * > <a href="https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/ece301-examples/CameraPreview.zip">CameraPreview.zip</a> (password preview).
 */
@SuppressLint("WrongThread") // TODEL when updated the Gradle plugin with new lint
@UiThread
public class CaptureImage extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
	private static final Logger LOG = LoggerFactory.getLogger(CaptureImage.class);
	public static final String EXTRA_OUTPUT = MediaStore.EXTRA_OUTPUT;
	public static final String EXTRA_OUTPUT_PUBLIC = MediaStore.EXTRA_OUTPUT + "-public";
	public static final String EXTRA_MAXSIZE = MediaStore.EXTRA_SIZE_LIMIT;
	public static final String EXTRA_QUALITY = "quality";
	public static final String EXTRA_FORMAT = "format";
	public static final String EXTRA_ASPECT = "keepAspect";
	public static final String EXTRA_SQUARE = "isSquare";
	public static final String EXTRA_FLASH = "flash";
	public static final String EXTRA_PICK = "pickImage";
	private static final String PREF_FLASH = EXTRA_FLASH;
	private static final String PREF_DENIED = "camera_permission_declined";
	private static final String KEY_STATE = "activityState";
	private static final String STATE_CAPTURING = "capturing";
	private static final String STATE_CROPPING = "cropping";
	private static final String STATE_PICKING = "picking";
	private static final float DEFAULT_MARGIN = 0.10f;
	private static final boolean DEFAULT_FLASH = false;
	public static final int EXTRA_MAXSIZE_NO_MAX = 0;
	public static final String ACTION = "net.twisterrob.android.intent.action.CAPTURE_IMAGE";

	private SharedPreferences prefs;

	private CameraPreview mPreview;
	/**
	 * If we set {@code mPreview.setVisibility(INVISIBLE)}, the camera is released. Acquiring it again takes ~1 second.
	 * When the user is taking an image, but not satisfied with it, starting the camera again is an unnecessary delay.
	 * To prevent this delay: don't hide {@code mPreview}, but draw over it by toggling the visibility of this view.
	 */
	private View mPreviewHider;
	private SelectionView mSelection;
	private File mTargetFile;
	private File mSavedFile;
	private ImageView mImage;
	private View controls;
	private String state;
	private ImageRequest request;

	private ImageButton mBtnCapture;
	private ImageButton mBtnPick;
	private ImageButton mBtnCrop;
	private ToggleButton mBtnFlash;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// FIXME fast 180 rotation results in flipped image: http://stackoverflow.com/a/19599599/253468 
		prefs = getPreferences(MODE_PRIVATE);

		String output = getIntent().getStringExtra(EXTRA_OUTPUT);
		if (output == null) {
			doReturn();
			return;
		} else {
			mTargetFile = new File(output);
		}

		setContentView(R.layout.activity_camera);
		controls = findViewById(R.id.controls);
		final View cameraControls = findViewById(R.id.camera_controls);
		mBtnPick = (ImageButton)controls.findViewById(R.id.btn_pick);
		mBtnCapture = (ImageButton)controls.findViewById(R.id.btn_capture);
		mBtnCrop = (ImageButton)controls.findViewById(R.id.btn_crop);
		mBtnFlash = (ToggleButton)cameraControls.findViewById(R.id.btn_flash);
		mPreview = (CameraPreview)findViewById(R.id.preview);
		mPreviewHider = findViewById(R.id.previewHider);
		mImage = (ImageView)findViewById(R.id.image);
		mSelection = (SelectionView)findViewById(R.id.selection);

		mPreview.setListener(new CameraPreviewListener() {
			@Override public void onCreate(CameraPreview preview) {
				mBtnFlash.setChecked(getInitialFlashEnabled()); // calls setOnCheckedChangeListener
			}
			@Override public void onResume(CameraPreview preview) {
				cameraControls.setVisibility(View.VISIBLE);
			}
			@TargetApi(VERSION_CODES.HONEYCOMB)
			@Override public void onShutter(CameraPreview preview) {
				final View flashView = mSelection;
				if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
					ObjectAnimator whiteFlashIn = ObjectAnimator.ofObject(flashView,
							"backgroundColor", new ArgbEvaluator(), 0x00FFFFFF, 0xAAFFFFFF);
					ObjectAnimator whiteFlashOut = ObjectAnimator.ofObject(flashView,
							"backgroundColor", new ArgbEvaluator(), 0xAAFFFFFF, 0x00000000);
					whiteFlashIn.setDuration(200);
					whiteFlashOut.setDuration(300);
					AnimatorSet whiteFlash = new AnimatorSet();
					whiteFlash.playSequentially(whiteFlashIn, whiteFlashOut);
					whiteFlash.addListener(new AnimatorListenerAdapter() {
						@SuppressWarnings("deprecation")
						@Override public void onAnimationEnd(Animator animation) {
							flashView.setBackgroundDrawable(null);
						}
					});
					whiteFlash.start();
				}
			}
			@Override public void onPause(CameraPreview preview) {
				cameraControls.setVisibility(View.INVISIBLE);
			}
			@Override public void onDestroy(CameraPreview preview) {
				// no op
			}
		});

		mSelection.setKeepAspectRatio(getIntent().getBooleanExtra(EXTRA_ASPECT, false));
		if (getIntent().getBooleanExtra(EXTRA_SQUARE, false)) {
			mSelection.setSelectionMarginSquare(DEFAULT_MARGIN);
		} else {
			mSelection.setSelectionMargin(DEFAULT_MARGIN);
		}

		mBtnFlash.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mPreview.setFlash(isChecked);
				prefs.edit().putBoolean(PREF_FLASH, isChecked).apply();
			}
		});

		mBtnCapture.setOnClickListener(new CaptureClickListener());
		mBtnPick.setOnClickListener(new PickClickListener());
		mBtnCrop.setOnClickListener(new CropClickListener());

		boolean hasCamera = ImageRequest.canHasCamera(this);
		if (!hasCamera) {
			mBtnCapture.setVisibility(View.GONE);
		}
		if (savedInstanceState == null) {
			boolean userDeclined = hasCamera && !hasCameraPermission() && prefs.getBoolean(PREF_DENIED, false);
			if (getIntent().getBooleanExtra(EXTRA_PICK, false) // forcing an immediate pick
					|| !hasCamera // device doesn't have camera
					|| userDeclined // device has camera, but user explicitly declined the permission
					) {
				mBtnPick.performClick();
			} else {
				mBtnCapture.performClick();
			}
		} else {
			state = savedInstanceState.getString(KEY_STATE);
			if (state != null) {
				switch (state) {
					case STATE_CAPTURING:
						mBtnCapture.performClick();
						break;
					case STATE_PICKING:
						// automatically restored
						break;
					case STATE_CROPPING:
						mSavedFile = mTargetFile;
						mSelection.setSelectionStatus(SelectionStatus.FOCUSED);
						prepareCrop();
						break;
				}
			}
		}
	}
	@Override protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_STATE, state);
	}
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			if (data != null && ACTION.equals(data.getAction())) {
				mBtnCapture.performClick();
			} else {
				mSelection.setSelectionStatus(SelectionStatus.BLURRY);
				enableControls();
			}
			return;
		}
		Uri fallback = Uri.fromFile(mTargetFile);
		Uri result = fallback;
		if (request != null) {
			Uri pic = request.getPictureUriFromResult(requestCode, resultCode, data);
			if (pic != null) {
				result = pic;
			}
		}
		if (!fallback.equals(result)) {
			try {
				LOG.trace("Loading image from {} to {}", result, mTargetFile);
				InputStream stream = getContentResolver().openInputStream(result);
				IOTools.copyStream(stream, new FileOutputStream(mTargetFile));
			} catch (IOException ex) {
				LOG.error("Cannot grab data from {} into {}", result, mTargetFile, ex);
			}
		}
		mSavedFile = mTargetFile;
		prepareCrop();
		enableControls();
	}

	private void prepareCrop() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			mImage.post(new Runnable() {
				@Override public void run() {
					prepareCrop();
				}
			});
			return;
		}
		state = STATE_CROPPING;
		LOG.trace("Loading taken image to crop: {}", mSavedFile);
		// Use a special target that will adjust the size of the ImageView to wrap the image (adjustViewBounds).
		// The selection view's size will match this hence the user can only select part of the image.
		// Used as listener to know if it's the thumbnail load or not, also needs skipMemoryCache to work
		ThumbWrapViewTarget<Bitmap> target = new ThumbWrapViewTarget<>(new BitmapImageViewTarget(mImage) {
			@Override public void setDrawable(Drawable drawable) {
				if (drawable instanceof TransitionDrawable) {
					// TODEL see https://github.com/bumptech/glide/issues/943
					((TransitionDrawable)drawable).setCrossFadeEnabled(false);
				}
				super.setDrawable(drawable);
			}
		});

		final SelectionStatus oldStatus = mSelection.getSelectionStatus();
		mSelection.setSelectionStatus(SelectionStatus.FOCUSING);
		RequestListener<Object, Bitmap> visualFeedbackListener = new RequestListener<Object, Bitmap>() {
			@Override public boolean onException(Exception e,
					Object model, Target<Bitmap> target, boolean isFirstResource) {
				mSelection.setSelectionStatus(SelectionStatus.BLURRY);
				return false;
			}
			@Override public boolean onResourceReady(Bitmap resource,
					Object model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
				if (oldStatus == SelectionStatus.BLURRY) {
					mSelection.setSelectionStatus(SelectionStatus.BLURRY);
				} else {
					mSelection.setSelectionStatus(SelectionStatus.FOCUSED);
				}
				return false;
			}
		};
		mPreviewHider.setVisibility(View.VISIBLE);
		GenericRequestBuilder<File, ImageVideoWrapper, Bitmap, Bitmap> image = Glide
				.with(this)
				.load(mSavedFile)
				.asBitmap() // no matter the format, just a single frame of bitmap
				.diskCacheStrategy(DiskCacheStrategy.NONE) // no need to cache, it's on disk already
				.skipMemoryCache(true) // won't ever be loaded again, or if it is, probably contains different bytes
				//.placeholder(new ColorDrawable(Color.BLACK)) // immediately hide the preview to prevent weird jump
				.transform(new FitCenter(GlideHelpers.NO_POOL)) // make sure full image is visible
				;

		@SuppressWarnings("unchecked")
		RequestListener<Object, Bitmap> listener = new MultiRequestListener<>(visualFeedbackListener, target);
		image
				.decoder(new NonPoolingImageVideoBitmapDecoder(DecodeFormat.PREFER_ARGB_8888))
				// don't lose quality (may be disabled to gain memory for crop)
				// need the special target/listener
				.thumbnail(image
						.clone() // inherit everything, but load lower quality
						.listener(target)
						.decoder(new NonPoolingImageVideoBitmapDecoder(DecodeFormat.PREFER_RGB_565))
						.sizeMultiplier(0.1f)
						.animate(android.R.anim.fade_in) // fade thumbnail in (=crossFade from background)
				)
				.listener(listener)
				.error(R.drawable.image_error)
				.animate(new BitmapCrossFadeFactory(150)) // fade from thumb to image
				.into(target)
		;
	}

	private boolean getInitialFlashEnabled() {
		boolean flash;
		if (getIntent().hasExtra(EXTRA_FLASH)) {
			flash = getIntent().getBooleanExtra(EXTRA_FLASH, DEFAULT_FLASH);
		} else {
			flash = prefs.getBoolean(PREF_FLASH, DEFAULT_FLASH);
		}
		return flash;
	}

	@WorkerThread
	protected void doSave(@Nullable byte... data) {
		mSavedFile = save(mTargetFile, data);
	}
	private void flipSelection() {
		if (Boolean.TRUE.equals(mPreview.isFrontFacing())) {
			Rect selection = mSelection.getSelection();
			int width = mSelection.getWidth();
			selection.left = width - selection.left;
			selection.right = width - selection.right;
			mSelection.setSelection(selection);
		}
	}
	@WorkerThread
	protected boolean doCrop(RectF rect) {
		try {
			mSavedFile = crop(mSavedFile, rect);
			return true;
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), "Cannot crop image: " + ex.getMessage(), Toast.LENGTH_LONG).show();
			LOG.warn("Cannot crop image file {}", mSavedFile, ex);
			return false;
		} catch (OutOfMemoryError ex) {
			// CONSIDER http://stackoverflow.com/a/26239077/253468, or other solution on the same question
			String message = "There's not enough memory to crop the image, sorry. Try a smaller selection.";
			LOG.warn(message, ex);
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			return false;
		}
	}
	protected void doRestartPreview() {
		if (requestCameraPermissionIfNeeded()) {
			mPreview.setVisibility(View.INVISIBLE);
			return;
		}
		if (STATE_CROPPING.equals(state)) {
			flipSelection();
		}
		state = STATE_CAPTURING;
		LOG.trace("Restarting preview");
		mPreviewHider.setVisibility(View.INVISIBLE);
		mPreview.setVisibility(View.VISIBLE);
		mSavedFile = null;
		mSelection.setSelectionStatus(SelectionStatus.NORMAL);
		mPreview.cancelTakePicture();
		Glide.clear(mImage);
		mImage.setImageDrawable(null); // remove Glide placeholder for the view to be transparent
		enableControls();
	}
	protected void doPick() {
		state = STATE_PICKING;
		mPreview.setVisibility(View.INVISIBLE);
		disableControls();
		mSelection.setSelectionStatus(SelectionStatus.FOCUSING);
		// TODO properly pass and handle EXTRA_OUTPUT as Uris
		Uri publicOutput = getIntent().getParcelableExtra(EXTRA_OUTPUT_PUBLIC);
		request = new ImageRequest.Builder(CaptureImage.this)
				.addGalleryIntent()
				.addCameraIntents(publicOutput != null? publicOutput : Uri.fromFile(mTargetFile))
				.build();
		request.start(); // continues in onActivityResult
	}

	private static final int PERMISSIONS_REQUEST_CAMERA = 1;
	private boolean requestCameraPermissionIfNeeded() {
		if (hasCameraPermission()) {
			return false;
		} else {
			// TODO if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) showDialog
			ActivityCompat.requestPermissions(this,
					new String[] {Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
			return true;
		}
	}
	@SuppressWarnings("NullableProblems") // doc says so
	@Override public void onRequestPermissionsResult(
			int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_CAMERA: {
				if (grantResults.length == 0) { // If request is cancelled, the result arrays are empty.
					break; // nothing we can do really, let's try again later when user interactions warrants it
				}
				// TODEL double-checking hasCameraPermission only needed while target API is below 23
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED && hasCameraPermission()) {
					// all ok, we have the permission, remember the user's approval
					prefs.edit().remove(PREF_DENIED).apply();
					doRestartPreview(); // start using the camera
				} else {
					// no permission: remember the user's disapproval, and don't bug again until explicit action
					prefs.edit().putBoolean(PREF_DENIED, true).apply();
					doPick(); // start picking as that's likely the action the user will need
				}
				break;
			}
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	private boolean hasCameraPermission() {
		int permissionState = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
		return permissionState == PermissionChecker.PERMISSION_GRANTED;
	}
	protected void doReturn() {
		if (mSavedFile != null) {
			Intent result = new Intent();
			result.setDataAndType(Uri.fromFile(mSavedFile), "image/jpeg");
			setResult(RESULT_OK, result);
		} else {
			setResult(RESULT_CANCELED, getIntent());
		}
		finish();
	}

	protected @CheckResult boolean take(final Callback<byte[]> jpegCallback) {
		LOG.trace("Initiate taking picture {}", mPreview.isRunning());
		if (!mPreview.isRunning()) {
			return false;
		}
		mSelection.setSelectionStatus(SelectionStatus.FOCUSING);
		mPreview.takePicture(new CameraPictureListener() {
			@Override public boolean onFocus(final boolean success) {
				LOG.trace("Auto-focus result: {}", success);
				//noinspection ResourceType post should be safe to call from background
				mSelection.post(new Runnable() {
					public void run() {
						mSelection.setSelectionStatus(success? SelectionStatus.FOCUSED : SelectionStatus.BLURRY);
					}
				});
				return true; // take the picture even if not in focus
			}
			@Override public void onTaken(@Nullable byte... image) {
				jpegCallback.call(image);
			}
		}, true);
		return true;
	}

	@WorkerThread
	private static @Nullable File save(@NonNull File file, @Nullable byte... data) {
		if (data == null) {
			return null;
		}
		LOG.trace("Saving {} bytes to {}", data.length, file);
		OutputStream out = null;
		try {
			//noinspection resource cannot use try-with-resources at this API level
			out = new FileOutputStream(file);
			out.write(data);
			out.flush();
			LOG.info("Raw image ({} bytes) saved at {}", data.length, file);
		} catch (FileNotFoundException ex) {
			LOG.error("Cannot find file {}", file, ex);
			file = null;
		} catch (IOException ex) {
			LOG.error("Cannot write file {}", file, ex);
			file = null;
		} finally {
			IOTools.ignorantClose(out);
		}
		return file;
	}

	@WorkerThread
	private File crop(File file, RectF sel) throws IOException {
		if (file == null || sel.isEmpty()) {
			return null;
		}
		final int[] originalSize = ImageTools.getSize(file);
		LOG.trace("Original image size: {}x{}", originalSize[0], originalSize[1]);

		// keep a single Bitmap variable so the rest could be garbage collected
		final int orientation = ImageTools.getExifOrientation(file);
		final RectF rotatedSel = ImageTools.rotateUnitRect(sel, orientation);
		final Rect imageRect = ImageTools.percentToSize(rotatedSel, originalSize[0], originalSize[1]);
		final int maxSize = getIntent().getIntExtra(EXTRA_MAXSIZE, EXTRA_MAXSIZE_NO_MAX);

		final float leeway = 0.10f;
		// calculating a sample size should speed up loading and lessen the probability of OOMs.
		final int sampleSize = maxSize == EXTRA_MAXSIZE_NO_MAX
				? 1 : calcSampleSize(maxSize, leeway, imageRect.width(), imageRect.height());
		LOG.trace("Downsampling by {}x", sampleSize);

		Bitmap bitmap = ImageTools.crop(file, imageRect, sampleSize);
		LOG.info("Cropped {} = {} to size {}x{}", sel, imageRect, bitmap.getWidth(), bitmap.getHeight());
		if (maxSize != EXTRA_MAXSIZE_NO_MAX) {
			bitmap = ImageTools.downscale(bitmap, maxSize, maxSize, leeway);
			LOG.info("Downscaled to size {}x{} by constraint {}±{}",
					bitmap.getWidth(), bitmap.getHeight(), maxSize, maxSize * leeway);
		}

		@Deprecated // experimental for now, don't enable; this would reduce OOMs even more,
		// because it would skip rotation which create a full copy of the bitmap
		// on the other hand, rotation should use less memory as saving (getPixels + YCC), so it may be unnecessary.
		final boolean exifRotate = Boolean.parseBoolean("false");
		ExifInterface exif = null;
		if (exifRotate) {
			exif = new ExifInterface(file.getAbsolutePath());
		} else {
			bitmap = ImageTools.rotateImage(bitmap, ImageTools.getExifRotation(orientation));
			LOG.info("Rotated to size {}x{} because {}({})",
					bitmap.getWidth(), bitmap.getHeight(), ImageTools.getExifString(orientation), orientation);
		}
		CompressFormat format = (CompressFormat)getIntent().getSerializableExtra(EXTRA_FORMAT);
		if (format == null) {
			format = CompressFormat.JPEG;
		}
		int quality = getIntent().getIntExtra(EXTRA_QUALITY, 85);

		ImageTools.savePicture(bitmap, format, quality, true, file);
		if (exifRotate) {
			exif.saveAttributes(); // restore original Exif (most importantly the orientation)
		}

		LOG.info("Saved {}x{} {}@{} into {}", bitmap.getWidth(), bitmap.getHeight(), format, quality, file);
		return file;
	}
	@AnyThread
	private int calcSampleSize(int maxSize, float leewayPercent, int sourceWidth, int sourceHeight) {
		// mirror calculations in ImageTools.downscale
		final float widthPercentage = maxSize / (float)sourceWidth;
		final float heightPercentage = maxSize / (float)sourceHeight;
		final float minPercentage = Math.min(widthPercentage, heightPercentage);

		final int targetWidth = Math.round(minPercentage * sourceWidth);
		final int targetHeight = Math.round(minPercentage * sourceHeight);
		LOG.trace("Downscale: {}x{} -> {}x{} ({}%) ± {}x{} ({}%)",
				sourceWidth, sourceHeight, targetWidth, targetHeight, minPercentage * 100,
				targetWidth * leewayPercent, targetHeight * leewayPercent, leewayPercent * 100);
		final int exactSampleSize = Math.min(sourceWidth / targetWidth, sourceHeight / targetHeight);
		int sampleSize = exactSampleSize <= 1? 1 : Integer.highestOneBit(exactSampleSize); // round down to 2^x
		LOG.trace("Chosen sample size based on size is {} rounded to {}", exactSampleSize, sampleSize);
		int longerSide = Math.max(sourceWidth, sourceHeight);
		int targetLongerSide = Math.max(targetWidth, targetHeight);
		if (Math.abs((float)longerSide / (sampleSize * 2) - targetLongerSide) < targetLongerSide * leewayPercent) {
			LOG.trace("The longer side {}px allows for leeway ({}%) of {}px when using sample size {}",
					longerSide, leewayPercent * 100, targetLongerSide * leewayPercent, sampleSize * 2);
			// this allows the loaded image size to be between [targetLongerSide * (1-leewayPercent), targetLongerSide]
			sampleSize = sampleSize * 2;
		}
		return sampleSize;
	}
	private @NonNull RectF getPictureRect() {
		float width = mSelection.getWidth();
		float height = mSelection.getHeight();

		RectF selection = new RectF(mSelection.getSelection());
		selection.left = selection.left / width;
		selection.top = selection.top / height;
		selection.right = selection.right / width;
		selection.bottom = selection.bottom / height;
		selection.sort();
		return selection;
	}

	/** @param maxSize pixel size or {@link #EXTRA_MAXSIZE_NO_MAX} */
	public static Intent saveTo(Context context, File targetFile, Uri publicTarget, int maxSize) {
		Intent intent = new Intent(context, CaptureImage.class);
		intent.putExtra(CaptureImage.EXTRA_OUTPUT, targetFile.getAbsolutePath());
		intent.putExtra(CaptureImage.EXTRA_OUTPUT_PUBLIC, publicTarget);
		intent.putExtra(CaptureImage.EXTRA_MAXSIZE, maxSize);
		return intent;
	}

	private class PickClickListener implements OnClickListener {
		@Override public void onClick(View v) {
			doPick();
		}
	}

	private class CaptureClickListener implements OnClickListener {
		@Override public void onClick(View v) {
			if (!mPreview.isRunning()) { // picked gallery, camera button -> enable preview
				doRestartPreview();
				return;
			}
			disableControls();
			if (mSavedFile == null) {
				if (!take(new Callback<byte[]>() {
					@Override public void call(@Nullable byte[] data) {
						doSave(data);
						flipSelection();
						prepareCrop();
						enableControls();
					}
				})) {
					String message = "Please enable camera before taking a picture.";
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				}
			} else {
				doRestartPreview();
			}
		}
	}

	private void enableControls() {
		// post, so everything has time to set up
		controls.post(new Runnable() {
			@Override public void run() {
				controls.setVisibility(View.VISIBLE);
			}
		});
	}
	private void disableControls() {
		// CONSIDER a grayscale colorfilter on the preview?
		controls.setVisibility(View.INVISIBLE);
	}

	private class CropClickListener implements OnClickListener {
		@Override public void onClick(View v) {
			final RectF selection = getPictureRect();
			Glide.clear(mImage); // free up memory for crop op
			if (mSavedFile != null) {
				if (doCrop(selection)) {
					doReturn();
				}
			} else {
				if (!take(new Callback<byte[]>() {
					@Override public void call(@Nullable byte[] data) {
						doSave(data);
						flipSelection();
						if (doCrop(selection)) {
							doReturn();
						}
					}
				})) {
					String message = "Please select or take a picture before cropping.";
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	private static class ThumbWrapViewTarget<Z> extends WrapViewTarget<Z> implements RequestListener<Object, Z> {
		private boolean isThumbnail;
		public ThumbWrapViewTarget(ImageViewTarget<? super Z> target) {
			super(target);
		}
		@Override public void onLoadStarted(Drawable placeholder) {
			super.onLoadStarted(placeholder);
			isThumbnail = false;
		}
		@Override public boolean onResourceReady(Z resource, Object model, Target<Z> target,
				boolean isFromMemoryCache, boolean isFirstResource) {
			this.isThumbnail = isFirstResource;
			return false; // normal route, just capture arguments
		}
		@Override public void onResourceReady(Z resource, GlideAnimation<? super Z> glideAnimation) {
			super.onResourceReady(resource, glideAnimation);
			if (isThumbnail) {
				update(LayoutParams.MATCH_PARENT);
			}
		}
		@Override public boolean onException(Exception e, Object model, Target<Z> target,
				boolean isFirstResource) {
			return false; // go for onLoadFailed
		}
	}
}
