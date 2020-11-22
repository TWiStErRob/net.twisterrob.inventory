package net.twisterrob.android.view;

import java.io.IOException;
import java.util.*;

import org.slf4j.*;

import android.annotation.*;
import android.content.Context;
import android.os.Build.*;
import android.os.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;

import net.twisterrob.android.utils.tools.AndroidTools;

@UiThread
@SuppressWarnings("deprecation")
// Deprecation warnings are constrained to class body by using FQCNs, because suppression doesn't work on imports.
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(CameraPreview.class);

	public interface CameraPreviewListener {
		void onCreate(CameraPreview preview);
		void onResume(CameraPreview preview);
		void onShutter(CameraPreview preview);
		void onPause(CameraPreview preview);
		void onDestroy(CameraPreview preview);
	}

	@WorkerThread
	public interface CameraPictureListener {
		/**
		 * @param success whether auto-focus succeeded.
		 *                If there's no auto-focus it always succeeds with {@code true}.
		 *                If the focus call failed with an exception it'll be {@code false}.
		 * @return whether you want to continue by taking a picture, {@code return success;} is a reasonable implementation
		 */
		boolean onFocus(boolean success);
		void onTaken(@Nullable byte... image);
	}

	private final MissedSurfaceEvents missedEvents = new MissedSurfaceEvents();
	private final CameraThreadHandler thread = new CameraThreadHandler();
	private @Nullable CameraHolder cameraHolder = null;
	private @NonNull CameraPreviewListeners listeners = new CameraPreviewListeners();

	public CameraPreview(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		LOG.trace("CameraPreview");

		getHolder().addCallback(this);
		getHolder().addCallback(thread);
		initCompat();
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	private void initCompat() {
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	public void addListener(@NonNull CameraPreviewListener listener) {
		listeners.add(listener);
	}

	public void removeListener(@NonNull CameraPreviewListener listener) {
		listeners.remove(listener);
	}

	public android.hardware.Camera getCamera() {
		return cameraHolder != null? cameraHolder.camera : null;
	}

	public boolean isRunning() {
		return cameraHolder != null;
	}

	@Override public void surfaceCreated(SurfaceHolder holder) {
		LOG.trace("{} surfaceCreated({})", cameraHolder, holder);
		if (cameraHolder != null) {
			usePreview();
		} else {
			missedEvents.surfaceCreated(holder);
		}
	}

	@Override public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		LOG.trace("{} surfaceChanged({}, format={}, w={}, h={})", cameraHolder, holder, format, w, h);
		if (cameraHolder != null) {
			stopPreview();
			updatePreview(w, h);
			startPreview();
		} else {
			missedEvents.surfaceChanged(holder, format, w, h);
		}
	}

	@Override public void surfaceDestroyed(SurfaceHolder holder) {
		LOG.trace("{} surfaceDestroyed({})", cameraHolder, holder);
		if (cameraHolder != null) {
			stopPreview();
			releaseCamera();
		} else {
			missedEvents.surfaceDestroyed(holder);
		}
	}

	private void usePreview() {
		LOG.trace("{} Using preview", cameraHolder);
		try {
			if (cameraHolder != null) {
				LOG.trace("setPreviewDisplay {}", getHolder());
				cameraHolder.camera.setPreviewDisplay(getHolder());
			}
		} catch (RuntimeException | IOException ex) {
			LOG.error("Error setting up camera preview", ex);
		}
	}

	private void updatePreview(int w, int h) {
		LOG.trace("{} Updating preview", cameraHolder);
		if (cameraHolder == null) {
			return;
		}

		int width = getWidth();
		int height = getHeight();
		int displayDegrees = AndroidTools.calculateDisplayOrientation(getContext(), cameraHolder.cameraInfo);
		int cameraDegrees = AndroidTools.calculateRotation(getContext(), cameraHolder.cameraInfo);
		boolean landscape = displayDegrees % 180 == 0;
		if (!landscape) {
			int temp = width;
			//noinspection SuspiciousNameCombination we need to swap them when orientation is portrait
			width = height;
			height = temp;
		}

		// if (cameraHolder.cameraInfo.facing == CAMERA_FACING_FRONT) setScaleX(-1); doesn't work
		// @see http://stackoverflow.com/a/10390407/253468#comment63748074_10390407

		android.hardware.Camera.Size previewSize =
				AndroidTools.getOptimalSize(cameraHolder.params.getSupportedPreviewSizes(), width, height);
		android.hardware.Camera.Size pictureSize =
				AndroidTools.getOptimalSize(cameraHolder.params.getSupportedPictureSizes(), width, height);
		LOG.debug("orient={}, rotate={}, landscape={}, "
						+ "size: {}x{} ({}), "
						+ "surface: {}x{} ({}), "
						+ "preview: {}x{} ({}), "
						+ "picture: {}x{} ({})",
				displayDegrees, cameraDegrees, landscape,
				width, height, (float)width / (float)height,
				w, h, (float)w / (float)h,
				previewSize.width, previewSize.height, (float)previewSize.width / (float)previewSize.height,
				pictureSize.width, pictureSize.height, (float)pictureSize.width / (float)pictureSize.height
		);
		cameraHolder.params.setPreviewSize(previewSize.width, previewSize.height);
		cameraHolder.params.setPictureSize(pictureSize.width, pictureSize.height);
		cameraHolder.params.setRotation(cameraDegrees);
		cameraHolder.params.set("orientation", landscape? "landscape" : "portrait");
		cameraHolder.camera.setParameters(cameraHolder.params);
		cameraHolder.camera.setDisplayOrientation(displayDegrees);
	}

	private void releaseCamera() {
		LOG.trace("{} Releasing camera", cameraHolder);
		if (cameraHolder != null) {
			// Important: Call release() to release the camera for use by other
			// applications. Applications should release the camera immediately
			// during onPause() and re-open() it during onResume()).
			LOG.info("Releasing {}", cameraHolder.camera);
			cameraHolder.camera.release();
			finished();
		}
	}

	private void startPreview() {
		LOG.trace("{} Starting preview", cameraHolder);
		try {
			if (cameraHolder != null) {
				cameraHolder.camera.startPreview();
				listeners.onResume(this);
			}
		} catch (RuntimeException ex) {
			LOG.error("Error starting camera preview", ex);
		}
	}

	private void stopPreview() {
		LOG.trace("{} Stopping preview", cameraHolder);
		try {
			if (cameraHolder != null) {
				cameraHolder.camera.stopPreview();
				listeners.onPause(this);
			}
		} catch (RuntimeException ex) {
			LOG.warn("ignore: tried to stop a non-existent preview", ex);
		}
	}

	private void started(CameraHolder holder) {
		cameraHolder = holder;
		missedEvents.replay();
		listeners.onCreate(this);
	}

	private void finished() {
		cameraHolder = null;
		listeners.onDestroy(this);
	}

	public Boolean isFrontFacing() {
		return cameraHolder == null? null
				: cameraHolder.cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	/**
	 * Initiate taking a picture, if {@code autoFocus} is {@code false} the picture will be taken immediately;
	 * otherwise an auto-focus will be triggered if possible.
	 * {@link CameraPictureListener#onFocus(boolean)} will be only called if auto-focus was requested.
	 * Taking the picture can be cancelled by {@link CameraPictureListener#onFocus(boolean) onFocus}.
	 */
	public void takePicture(final CameraPictureListener callback, boolean autoFocus) {
		LOG.trace("{} Taking picture", cameraHolder);
		if (cameraHolder == null) {
			return;
		}
		if (autoFocus) {
			focus(callback);
			return;
		}
		try {
			cameraHolder.camera.takePicture(new android.hardware.Camera.ShutterCallback() {
				@Override public void onShutter() {
					post(new Runnable() {
						@Override public void run() {
							listeners.onShutter(CameraPreview.this);
						}
					});
					post(new Runnable() {
						@Override public void run() {
							listeners.onPause(CameraPreview.this);
						}
					});
				}
			}, null, null, new android.hardware.Camera.PictureCallback() {
				@Override public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
					callback.onTaken(data);
				}
			});
		} catch (RuntimeException ex) {
			LOG.warn("Cannot take picture", ex);
			thread.post(new Runnable() {
				@Override public void run() {
					callback.onTaken((byte[])null);
				}
			});
		}
	}

	public void cancelTakePicture() {
		LOG.trace("Cancel take picture");
		cancelAutoFocus();
		startPreview();
	}

	private void cancelAutoFocus() {
		LOG.trace("{} Cancel auto-focus", cameraHolder);
		if (cameraHolder != null) {
			cameraHolder.camera.cancelAutoFocus();
		}
	}

	/**
	 * Make the camera's picture be focused (if supported) and take a picture if the callback returns true.
	 * @see CameraPictureListener#onFocus(boolean)
	 */
	public void focus(final CameraPictureListener callback) {
		LOG.trace("{} Camera focus", cameraHolder);
		if (cameraHolder == null) {
			return;
		}
		android.hardware.Camera.AutoFocusCallback cameraCallback = new android.hardware.Camera.AutoFocusCallback() {
			@Override public void onAutoFocus(boolean success, android.hardware.Camera camera) {
				if (callback.onFocus(success)) {
					takePicture(callback, false);
				}
			}
		};
		String focusMode = cameraHolder.camera.getParameters().getFocusMode();
		if (android.hardware.Camera.Parameters.FOCUS_MODE_AUTO.equals(focusMode)
				|| android.hardware.Camera.Parameters.FOCUS_MODE_MACRO.equals(focusMode)) {
			try {
				cameraHolder.camera.autoFocus(cameraCallback);
			} catch (RuntimeException ex) {
				LOG.warn("Failed to autofocus", ex);
				cameraCallback.onAutoFocus(false, cameraHolder.camera);
			}
		} else {
			cameraCallback.onAutoFocus(true, cameraHolder.camera);
		}
	}

	public Boolean isFlashSupported() {
		if (cameraHolder == null) {
			return null;
		}
		List<String> modes = cameraHolder.params.getSupportedFlashModes();
		if (modes != null) {
			return modes.contains(android.hardware.Camera.Parameters.FLASH_MODE_ON)
					&& modes.contains(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
		} else {
			// no flash support, e.g.
			// Samsung Galaxy Tab A (gta3xlwifi), 3072MB RAM, Android 9
			// Huawei HUAWEI MediaPad M5 Pro (HWCMR), 3840MB RAM, Android 9
			// LGE Qua tab PX (b3), 2048MB RAM, Android 7.0
			return false;
		}
	}

	/**
	 * @see #isFlashSupported() as a guard, {@code setFlashMode} crashes otherwise.
	 */
	public void setFlash(boolean flash) {
		if (cameraHolder == null && !Boolean.TRUE.equals(isFlashSupported())) {
			return;
		}
		String flashMode = flash
				? android.hardware.Camera.Parameters.FLASH_MODE_ON
				: android.hardware.Camera.Parameters.FLASH_MODE_OFF;
		cameraHolder.params.setFlashMode(flashMode);
		cameraHolder.camera.setParameters(cameraHolder.params);
	}

	private static class CameraHolder {
		final int cameraID;
		final android.hardware.Camera camera;
		final android.hardware.Camera.CameraInfo cameraInfo;
		final android.hardware.Camera.Parameters params;

		public CameraHolder(int id) {
			cameraID = id;
			LOG.trace("Opening camera");
			camera = android.hardware.Camera.open(cameraID);
			LOG.trace("Opened camera");
			try {
				LOG.trace("setPreviewDisplay null");
				camera.setPreviewDisplay(null);
			} catch (RuntimeException | IOException ex) {
				LOG.error("Error setting up camera preview", ex);
			}
			cameraInfo = new android.hardware.Camera.CameraInfo();
			params = camera.getParameters();
			android.hardware.Camera.getCameraInfo(cameraID, cameraInfo);
		}

		@Override public @NonNull String toString() {
			return String.format(Locale.ROOT, "Camera #%d (%s, %d°)",
					cameraID, facing(cameraInfo.facing), cameraInfo.orientation);
		}
		private static String facing(int facing) {
			switch (facing) {
				case android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK:
					return "back";
				case android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT:
					return "front";
				default:
					return "unknown";
			}
		}
	}

	private class CameraHandlerThread extends HandlerThread {
		private final Looper mLooper;
		private final Handler mHandler;

		@MainThread
		public CameraHandlerThread() {
			super("CameraHandlerThread");
			start();
			mLooper = getLooper();
			mHandler = new Handler(mLooper);
		}

		public void startOpenCamera() {
			mHandler.post(new Runnable() {
				// Interprocedural thread annotation violation (WorkerThread to UiThread): anon#run -> View#post
				// CONSIDER no idea what's wrong with this, looks legit usage to me
				@SuppressLint("WrongThreadInterprocedural")
				@WorkerThread
				@Override public void run() {
					int cameraID = findCamera();
					try {
						final CameraHolder holder = new CameraHolder(cameraID);
						CameraPreview.this.post(new Runnable() {
							@UiThread
							public void run() {
								CameraPreview.this.started(holder);
							}
						});
					} catch (final RuntimeException ex) {
						LOG.error("Error setting up camera #{}", cameraID, ex);
						CameraPreview.this.post(new Runnable() {
							@UiThread
							public void run() {
								Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
								setVisibility(View.INVISIBLE); // destroy surface for callbacks to trigger
							}
						});
					}
				}

				private int findCamera() {
					int cameras = android.hardware.Camera.getNumberOfCameras();
					int frontId = -1;
					int backId = -1;
					for (int i = 0; i < cameras; ++i) {
						android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
						android.hardware.Camera.getCameraInfo(0, info);
						if (backId == -1 && info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
							backId = i;
						}
						if (frontId == -1 && info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
							frontId = i;
						}
					}
					int id = backId != -1? backId : frontId;
					return id != -1? id : 0;
				}
			});
		}

		public void stopThread() {
			mLooper.quit();
		}
		public void post(Runnable runnable) {
			mHandler.post(runnable);
		}
	}

	private class CameraThreadHandler implements SurfaceHolder.Callback {
		private CameraHandlerThread mCameraThread = null;
		@Override public void surfaceCreated(SurfaceHolder holder) {
			if (mCameraThread != null) {
				throw new IllegalStateException("Camera Thread already started");
			}
			mCameraThread = new CameraHandlerThread();
			LOG.trace("Starting thread {}", mCameraThread);
			mCameraThread.startOpenCamera();
		}
		@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		}
		@Override public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCameraThread != null) {
				LOG.trace("Stopping thread {}", mCameraThread);
				mCameraThread.stopThread();
				mCameraThread = null;
			}
		}
		public void post(Runnable runnable) {
			mCameraThread.post(runnable);
		}
	}

	private class MissedSurfaceEvents implements SurfaceHolder.Callback {
		private boolean surfaceCreated;
		private boolean surfaceChanged;
		private boolean surfaceDestroyed;
		private SurfaceHolder holder;
		private int format;
		private int w, h;

		public void surfaceCreated(SurfaceHolder holder) {
			this.surfaceCreated = true;
			this.holder = holder;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			this.surfaceChanged = true;
			this.holder = holder;
			this.format = format;
			this.w = width;
			this.h = height;
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			this.surfaceDestroyed = true;
			this.holder = holder;
		}

		public void replay() {
			if (surfaceDestroyed) {
				CameraPreview.this.surfaceDestroyed(holder);
				return;
			}
			if (surfaceCreated) {
				CameraPreview.this.surfaceCreated(holder);
			}
			if (surfaceChanged) {
				CameraPreview.this.surfaceChanged(holder, format, w, h);
			}
		}
	}

	private static class CameraPreviewListeners implements CameraPreviewListener {

		private final @NonNull Collection<CameraPreviewListener> listeners = new LinkedHashSet<>();

		public void add(@NonNull CameraPreviewListener listener) {
			listeners.add(listener);
		}

		public void remove(@NonNull CameraPreviewListener listener) {
			listeners.remove(listener);
		}

		@Override public void onCreate(CameraPreview preview) {
			for (CameraPreviewListener listener : listeners) {
				listener.onCreate(preview);
			}
		}
		@Override public void onResume(CameraPreview preview) {
			for (CameraPreviewListener listener : listeners) {
				listener.onResume(preview);
			}
		}
		@Override public void onShutter(CameraPreview preview) {
			for (CameraPreviewListener listener : listeners) {
				listener.onShutter(preview);
			}
		}
		@Override public void onPause(CameraPreview preview) {
			for (CameraPreviewListener listener : listeners) {
				listener.onPause(preview);
			}
		}
		@Override public void onDestroy(CameraPreview preview) {
			for (CameraPreviewListener listener : listeners) {
				listener.onDestroy(preview);
			}
		}
	}
}
