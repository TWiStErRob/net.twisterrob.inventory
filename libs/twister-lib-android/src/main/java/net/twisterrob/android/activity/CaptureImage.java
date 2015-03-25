package net.twisterrob.android.activity;

import java.io.*;

import org.slf4j.*;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.twisterrob.android.R;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.view.*;
import net.twisterrob.android.view.CameraPreview.CameraPreviewListener;
import net.twisterrob.android.view.SelectionView.SelectionStatus;
import net.twisterrob.java.io.IOTools;

@SuppressWarnings("deprecation")
public class CaptureImage extends Activity {
	private static final Logger LOG = LoggerFactory.getLogger(CaptureImage.class);
	private static final String EXTRA_OUTPUT = MediaStore.EXTRA_OUTPUT;
	private static final String EXTRA_ASPECT = "keepAspect";
	private static final String EXTRA_SQUARE = "isSquare";
	private static final String EXTRA_FLASH = "flash";
	private static final String PREF_FLASH = EXTRA_FLASH;
	private static final float DEFAULT_MARGIN = 0.10f;
	private static final boolean DEFAULT_FLASH = false;

	private CameraPreview mPreview;
	private SelectionView mSelection;
	private File mTargetFile;
	private File mSavedFile;
	private ImageView mImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		String output = getIntent().getStringExtra(EXTRA_OUTPUT);
		if (output == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		} else {
			mTargetFile = new File(output);
		}

		setContentView(R.layout.activity_camera);
		final View controls = findViewById(R.id.controls);
		final ImageButton btnCapture = (ImageButton)controls.findViewById(R.id.btn_capture);
		final ImageButton btnCrop = (ImageButton)controls.findViewById(R.id.btn_crop);
		final ToggleButton btnFlash = (ToggleButton)controls.findViewById(R.id.btn_flash);
		mPreview = (CameraPreview)findViewById(R.id.preview);
		mImage = (ImageView)findViewById(R.id.image);
		mSelection = (SelectionView)findViewById(R.id.selection);

		btnFlash.setVisibility(View.INVISIBLE); // hide until it can operate
		mPreview.setListener(new CameraPreviewListener() {
			@Override public void onStarted(CameraPreview preview) {
				btnFlash.setVisibility(View.VISIBLE);
				btnFlash.setChecked(getInitialFlashEnabled()); // calls setOnCheckedChangeListener
			}
			@Override public void onFinished(CameraPreview preview) {
			}
		});

		mSelection.setKeepAspectRatio(getIntent().getBooleanExtra(EXTRA_ASPECT, false));
		if (getIntent().getBooleanExtra(EXTRA_SQUARE, false)) {
			mSelection.setSelectionMarginSquare(DEFAULT_MARGIN);
		} else {
			mSelection.setSelectionMargin(DEFAULT_MARGIN);
		}

		btnFlash.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mPreview.setFlash(isChecked);
				getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_FLASH, isChecked).apply();
			}
		});

		btnCapture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableControls();
				if (mSavedFile == null) {
					take(new Camera.PictureCallback() {
						public void onPictureTaken(byte[] data, Camera camera) {
							doSave(data);
							prepareCrop();
							enableControls();
						}
					});
				} else {
					doRestartPreview();
					enableControls();
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
				controls.setVisibility(View.INVISIBLE);
			}
		});

		btnCrop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mSavedFile != null) {
					doCrop();
					doReturn();
				} else {
					take(new Camera.PictureCallback() {
						public void onPictureTaken(byte[] data, Camera camera) {
							doSave(data);
							doCrop();
							doReturn();
						}
					});
				}
			}
		});
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
		LOG.trace("Loading taken image to crop: {}", mSavedFile);
		Glide
				.with(CaptureImage.this)
				.load(mSavedFile)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.skipMemoryCache(true)
				.placeholder(new ColorDrawable(Color.BLACK))
				.thumbnail(0.1f)
				.dontAnimate()
				.into(mImage);
	}

	private boolean getInitialFlashEnabled() {
		boolean flash;
		if (getIntent().hasExtra(EXTRA_FLASH)) {
			flash = getIntent().getBooleanExtra(EXTRA_FLASH, DEFAULT_FLASH);
		} else {
			flash = getPreferences(MODE_PRIVATE).getBoolean(PREF_FLASH, DEFAULT_FLASH);
		}
		return flash;
	}

	protected void doSave(byte[] data) {
		mSavedFile = save(mTargetFile, data);
	}
	protected void doCrop() {
		mSavedFile = crop(mSavedFile);
	}
	protected void doRestartPreview() {
		LOG.trace("Restarting preview");
		mSavedFile = null;
		mSelection.setSelectionStatus(SelectionStatus.NORMAL);
		mPreview.cancelTakePicture();
		Glide.clear(mImage);
		mImage.setImageDrawable(null);
	}
	protected void doReturn() {
		Intent result = new Intent();
		result.setDataAndType(Uri.fromFile(mSavedFile), "image/jpeg");
		setResult(RESULT_OK, result);
		finish();
	}
	protected void take(final Camera.PictureCallback jpegCallback) {
		LOG.trace("Initiate taking picture {}", mPreview.isRunning());
		if (!mPreview.isRunning()) {
			return;
		}
		mSelection.setSelectionStatus(SelectionStatus.FOCUSING);
		mPreview.setCameraFocus(new Camera.AutoFocusCallback() {
			public void onAutoFocus(final boolean success, Camera camera) {
				LOG.trace("Auto-focus result: {}", success);
				mSelection.post(new Runnable() {
					public void run() {
						mSelection.setSelectionStatus(success? SelectionStatus.FOCUSED : SelectionStatus.BLURRY);
					}
				});
				mPreview.takePicture(jpegCallback);
			}
		});
	}

	private static File save(File file, byte[] data) {
		if (data == null) {
			return null;
		}
		LOG.trace("Saving {} bytes to {}", data.length, file);
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(data);
			LOG.info("Raw image saved at {}", file);
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

	private File crop(File file) {
		try {
			RectF sel = getPictureRect();
			if (file != null && !sel.isEmpty()) {
				Bitmap bitmap = ImageTools.cropPicture(file, sel.left, sel.top, sel.right, sel.bottom);
				ImageTools.savePicture(bitmap, file, CompressFormat.JPEG, 80);
				LOG.info("Cropped file saved at {}", file);
				return file;
			}
		} catch (IOException ex) {
			LOG.error("Cannot crop image file {}", file, ex);
		}
		return null;
	}

	private RectF getPictureRect() {
		float width = mSelection.getWidth();
		float height = mSelection.getHeight();

		RectF selection = new RectF(mSelection.getSelection());
		selection.left = selection.left / width;
		selection.top = selection.top / height;
		selection.right = selection.right / width;
		selection.bottom = selection.bottom / height;
		return selection;
	}

	public static Intent saveTo(Context context, File targetFile) {
		Intent intent = new Intent(context, CaptureImage.class);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, targetFile.getAbsolutePath());
		PackageManager pm = context.getPackageManager();
		if (!AndroidTools.hasPermission(context, Manifest.permission.CAMERA)) {
			throw new IllegalStateException("Camera permission is not granted, please add it to your manifest:\n"
					+ "<uses-permission android:name=\"android.permission.CAMERA\" />");
		}
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			throw new IllegalStateException("Sorry, this system doesn't have a camera.");
		}
		return intent;
	}
}
