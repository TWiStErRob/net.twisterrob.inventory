package net.twisterrob.inventory.android.activity;

import java.io.*;

import org.slf4j.*;

import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.*;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.utils.PictureUtils;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.java.io.IOTools;

public class CaptureImage extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(CaptureImage.class);
	private CameraPreview mPreview;
	private SelectionView mSelection;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getSupportActionBar().hide();

		setContentView(R.layout.camera);

		mPreview = (CameraPreview)findViewById(R.id.preview);
		mSelection = (SelectionView)findViewById(R.id.selection);
		mSelection.setKeepAspectRatio(true);
		mSelection.setSelectionMarginSquare(0.10f); // 10 % off short side

		findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Camera camera = mPreview.getCamera();
				if (camera == null) {
					return;
				}
				camera.takePicture(new ShutterCallback() {
					public void onShutter() {
						LOG.trace("shutter");
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera camera) {
						LOG.trace("raw {}", data == null? -1 : data.length);
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera camera) {
						LOG.trace("postview {}", data == null? -1 : data.length);
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera camera) {
						LOG.trace("jpeg {}", data == null? -1 : data.length);
						File file = new File(getExternalCacheDir(), "picture.jpg");
						@SuppressWarnings("resource")
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

						try {
							Rect selection = mSelection.getSelection();
							if (file != null && selection != null) {
								Bitmap bitmap = PictureUtils.cropPicture(file, selection.left, selection.top,
										selection.width(), selection.height());
								PictureUtils.savePicture(bitmap, file, CompressFormat.JPEG, 80);
								LOG.info("Cropped file saved at {}", file);
							}
						} catch (IOException ex) {
							LOG.error("Cannot crop image file {}", file, ex);
							file = null;
						}
						if (file != null) {
							Toast.makeText(CaptureImage.this, "Picture saved to " + file, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
	}
}
