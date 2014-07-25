package net.twisterrob.inventory.android.view;

import java.io.IOException;
import java.util.List;

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
	private List<Size> mSupportedPreviewSizes;

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
		if (mCamera != null) {
			setupOrientation();
			updateParameters(w, h);
		}
	}

	private void updateParameters(int w, int h) {
		LOG.debug("size: {}x{}, surface: {}x{}", getWidth(), getHeight(), w, h);
		Camera.Parameters parameters = mCamera.getParameters();
		Camera.Size mPreviewSize = AndroidTools.getOptimalPreviewSize(mSupportedPreviewSizes, getWidth(), getHeight());
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		mCamera.setParameters(parameters);
	}

	private void setupOrientation() {
		WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		int rotation = windowManager.getDefaultDisplay().getRotation();
		int degrees = rotation * 90; // consider using Surface.ROTATION_ constants

		int result;
		if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (mCameraInfo.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (mCameraInfo.orientation - degrees + 360) % 360;
		}
		mCamera.setDisplayOrientation(result);
	}

	private void acquireCamera() {
		if (mCamera != null) {
			throw new IllegalStateException("Camera already acquired");
		}
		int mCameraID = 0;
		mCamera = Camera.open(mCameraID);
		mCameraInfo = new CameraInfo();
		Camera.getCameraInfo(mCameraID, mCameraInfo);

		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
		for (Camera.Size str: mSupportedPreviewSizes) {
			LOG.trace("Supported preview size: {}x{}", str.width, str.height);
		}
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
			mSupportedPreviewSizes = null;
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

	public void setCameraFocus(AutoFocusCallback autoFocus) {
		String focusMode = mCamera.getParameters().getFocusMode();
		if (Parameters.FOCUS_MODE_AUTO.equals(focusMode) || Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
			mCamera.autoFocus(autoFocus);
		}
	}

	public void setFlash(boolean flash) {
		Parameters mParameters = mCamera.getParameters();
		Toast.makeText(getContext(), "Flash is: " + mParameters.getFlashMode(), Toast.LENGTH_LONG).show();
		if (flash) {
			mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(mParameters);
		} else {
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(mParameters);
		}
	}

	// TODO implement onMeasure or drop this
	@SuppressLint("WrongCall")
	private void onMeasureTODO(int widthMeasureSpec, int heightMeasureSpec) {
		LOG.trace("onMeasure({}, {})", widthMeasureSpec, heightMeasureSpec);

		if (mSupportedPreviewSizes == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}

		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

		Size mPreviewSize = AndroidTools.getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
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