package net.twisterrob.inventory.android.activity;

import java.io.*;

import org.slf4j.*;

import android.content.Intent;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.*;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.utils.PictureUtils;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.SelectionView.SelectionStatus;
import net.twisterrob.java.io.IOTools;

public class CaptureImage extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(CaptureImage.class);

	private CameraPreview mPreview;
	private SelectionView mSelection;
	private File mTargetFile;
	private File mSavedFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getSupportActionBar().hide();

		String output = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
		if (output == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		} else {
			mTargetFile = new File(output);
		}

		setContentView(R.layout.camera_activity);

		ImageButton btnCapture = (ImageButton)findViewById(R.id.btn_capture);
		ImageButton btnCrop = (ImageButton)findViewById(R.id.btn_crop);
		ToggleButton btnFlash = (ToggleButton)findViewById(R.id.btn_flash);
		mPreview = (CameraPreview)findViewById(R.id.preview);
		mSelection = (SelectionView)findViewById(R.id.selection);

		mSelection.setKeepAspectRatio(true);
		mSelection.setSelectionMarginSquare(0.10f); // 10 % off short side

		btnFlash.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mPreview.setFlash(isChecked);
			}
		});

		btnCapture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSavedFile == null) {
					take(new PictureCallback() {
						public void onPictureTaken(byte[] data, Camera camera) {
							doSave(data);
						}
					});
				} else {
					doRestartPreview();
				}
			}
		});

		btnCrop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mSavedFile != null) {
					doCrop();
					doFinish();
				} else {
					take(new PictureCallback() {
						public void onPictureTaken(byte[] data, Camera camera) {
							doSave(data);
							doCrop();
							doFinish();
						}
					});
				}
			}

		});
	}
	protected void doSave(byte[] data) {
		mSavedFile = save(data);
	}
	protected void doCrop() {
		mSavedFile = crop(mSavedFile);
	}
	protected void doRestartPreview() {
		mSavedFile = null;
		mSelection.setSelectionStatus(SelectionStatus.NORMAL);
		mPreview.cancelTakePicture();
	}
	protected void doFinish() {
		Intent result = new Intent();
		result.setDataAndType(Uri.fromFile(mSavedFile), "image/jpeg");
		setResult(RESULT_OK, result);
		finish();
	}
	protected void take(final PictureCallback jpegCallback) {
		if (!mPreview.isRunning()) {
			return;
		}
		mPreview.setCameraFocus(new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				if (success) {
					mSelection.setSelectionStatus(SelectionStatus.FOCUSED);
				} else {
					mSelection.setSelectionStatus(SelectionStatus.BLURRY);
				}
				mPreview.takePicture(jpegCallback);
			}
		});
	}

	@SuppressWarnings("resource")
	private File save(byte[] data) {
		if (data == null) {
			return null;
		}
		File file = mTargetFile;
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			IOTools.writeAll(out, data);
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
				Bitmap bitmap = PictureUtils.cropPicture(file, sel.left, sel.top, sel.right, sel.bottom);
				PictureUtils.savePicture(bitmap, file, CompressFormat.JPEG, 80);
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
}
