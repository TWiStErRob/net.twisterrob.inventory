package net.twisterrob.inventory.android.view;

import java.io.IOException;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.*;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.view.*;
import android.widget.Toast;

import net.twisterrob.android.utils.tools.AndroidTools;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(CameraPreview.class);

	private Camera mCamera;
	private CameraInfo mCameraInfo;
	private Parameters mParams;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		LOG.trace("CameraPreview");

		getHolder().addCallback(this);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	public Camera getCamera() {
		return mCamera;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		LOG.trace("surfaceCreated({})", holder);
		acquireCamera();
		usePreview();

		startPreview(); // so that surfaceChanged can stop it without exception
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LOG.trace("surfaceDestroyed({})", holder);
		stopPreview();
		releaseCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		LOG.trace("surfaceChanged({}, format={}, w={}, h={})", holder, format, w, h);
		stopPreview();
		updatePreview(w, h);
		startPreview();
	}

	private void updatePreview(int w, int h) {
		if (mCamera == null) {
			return;
		}

		int width = getWidth();
		int height = getHeight();
		int degrees = AndroidTools.calculateRotation(getContext(), mCameraInfo);
		boolean landscape = degrees % 180 == 0;
		if (!landscape) {
			int temp = width;
			width = height;
			height = temp;
		}

		Size previewSize = AndroidTools.getOptimalSize(mParams.getSupportedPreviewSizes(), width, height);
		Size pictureSize = AndroidTools.getOptimalSize(mParams.getSupportedPictureSizes(), width, height);
		LOG.debug("orient: {}, size: {}x{} ({}), surface: {}x{} ({}), preview: {}x{} ({}), picture: {}x{} ({})", //
				degrees, //
				width, height, (float)width / (float)height, //
				w, h, (float)w / (float)h, //
				previewSize.width, previewSize.height, (float)previewSize.width / (float)previewSize.height, //
				pictureSize.width, pictureSize.height, (float)pictureSize.width / (float)pictureSize.height //
		);

		mParams.setPreviewSize(previewSize.width, previewSize.height);
		mParams.setPictureSize(pictureSize.width, pictureSize.height);
		mParams.setRotation(degrees);
		mParams.set("orientation", landscape? "landscape" : "portrait");
		mCamera.setParameters(mParams);
		mCamera.setDisplayOrientation(degrees);
	}

	private void acquireCamera() {
		if (mCamera != null) {
			throw new IllegalStateException("Camera already acquired");
		}
		int mCameraID = 0; // TODO front camera?
		mCamera = Camera.open(mCameraID);
		mCameraInfo = new CameraInfo();
		mParams = mCamera.getParameters();
		Camera.getCameraInfo(mCameraID, mCameraInfo);
	}

	private void releaseCamera() {
		if (mCamera != null) {
			// Important: Call release() to release the camera for use by other
			// applications. Applications should release the camera immediately
			// during onPause() and re-open() it during onResume()).
			LOG.info("Releasing {}", mCamera);
			mCamera.release();
			mCamera = null;
			mCameraInfo = null;
			mParams = null;
		}
	}

	private void usePreview() {
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(getHolder());
			}
		} catch (RuntimeException ex) {
			LOG.error("Error setting up camera preview", ex);
		} catch (IOException ex) {
			LOG.error("Error setting up camera preview", ex);
		}
	}

	private void startPreview() {
		try {
			if (mCamera != null) {
				mCamera.startPreview();
			}
		} catch (RuntimeException ex) {
			LOG.error("Error starting camera preview", ex);
		}
	}

	private void stopPreview() {
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		} catch (RuntimeException ex) {
			LOG.warn("ignore: tried to stop a non-existent preview", ex);
		}
	}

	public void cancelTakePicture() {
		startPreview();
	}

	public void setCameraFocus(AutoFocusCallback autoFocus) {
		String focusMode = mCamera.getParameters().getFocusMode();
		if (Parameters.FOCUS_MODE_AUTO.equals(focusMode) || Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
			mCamera.autoFocus(autoFocus);
		}
	}

	public void setFlash(boolean flash) {
		if (mCamera == null) {
			return;
		}
		String flashMode;
		if (flash) {
			flashMode = Parameters.FLASH_MODE_ON;
		} else {
			flashMode = Parameters.FLASH_MODE_OFF;
		}
		mParams.setFlashMode(flashMode);
		mCamera.setParameters(mParams);
		Toast.makeText(getContext(), "Flash is: " + mParams.getFlashMode(), Toast.LENGTH_SHORT).show();
	}

	// TODO implement onMeasure or drop this
	@SuppressLint("WrongCall")
	private void onMeasureTODO(int widthMeasureSpec, int heightMeasureSpec) {
		LOG.trace("onMeasure({}, {})", widthMeasureSpec, heightMeasureSpec);

		if (mCamera == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		Size mPreviewSize = AndroidTools.getOptimalSize(mParams.getSupportedPreviewSizes(), width, height);
		LOG.debug("Chosen optimal preview size: {}x{}", mPreviewSize.width, mPreviewSize.height);

		float ratio;
		if (mPreviewSize.height >= mPreviewSize.width) {
			ratio = (float)mPreviewSize.height / (float)mPreviewSize.width;
		} else {
			ratio = (float)mPreviewSize.width / (float)mPreviewSize.height;
		}

		// One of these methods should be used, second method squishes preview slightly
		setMeasuredDimension(width, (int)(width * ratio));
		//setMeasuredDimension((int) (width * ratio), height);
	}
}