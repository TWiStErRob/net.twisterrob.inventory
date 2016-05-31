package net.twisterrob.android.view;

import java.io.IOException;
import java.util.Locale;

import org.slf4j.*;

import android.annotation.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build.*;
import android.os.*;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.Toast;

import net.twisterrob.android.utils.tools.AndroidTools;

@UiThread
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(CameraPreview.class);

	public interface CameraPreviewListener {
		void onCreate(CameraPreview preview);
		void onResume(CameraPreview preview);
		void onShutter(CameraPreview preview);
		void onPause(CameraPreview preview);
		void onDestroy(CameraPreview preview);
	}

	public static class CameraPreviewListenerAdapter implements CameraPreviewListener {
		@Override public void onCreate(CameraPreview preview) {
			// optional override
		}
		@Override public void onResume(CameraPreview preview) {
			// optional override
		}
		@Override public void onShutter(CameraPreview preview) {
			// optional override
		}
		@Override public void onPause(CameraPreview preview) {
			// optional override
		}
		@Override public void onDestroy(CameraPreview preview) {
			// optional override
		}
	}

	@WorkerThread
	public interface CameraPictureListener {
		boolean onFocus(boolean success);
		void onTaken(byte... image);
	}

	private final MissedSurfaceEvents missedEvents = new MissedSurfaceEvents();
	private CameraHolder cameraHolder = null;
	private @NonNull CameraPreviewListener listener = new CameraPreviewListenerAdapter();

	public CameraPreview(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		LOG.trace("CameraPreview");

		getHolder().addCallback(this);
		getHolder().addCallback(new CameraThreadHandler());
		initCompat();
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	private void initCompat() {
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	@SuppressWarnings("deprecation")
	public boolean canHasCamera(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
				&& android.hardware.Camera.getNumberOfCameras() > 0;
	}

	public void setListener(@Nullable CameraPreviewListener listener) {
		this.listener = listener != null? listener : new CameraPreviewListenerAdapter();
	}

	public @SuppressWarnings("deprecation") android.hardware.Camera getCamera() {
		return cameraHolder != null? cameraHolder.camera : null;
	}

	public boolean isRunning() {
		return cameraHolder != null;
	}

	@Override public void surfaceCreated(SurfaceHolder holder) {
		LOG.trace("{} surfaceCreated({}) {}", cameraHolder, holder);
		if (cameraHolder != null) {
			usePreview();
		} else {
			missedEvents.surfaceCreated(holder);
		}
	}

	@Override public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		LOG.trace("{} surfaceChanged({}, format={}, w={}, h={}) {}", cameraHolder, holder, format, w, h);
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
		int degrees = AndroidTools.calculateRotation(getContext(), cameraHolder.cameraInfo);
		boolean landscape = degrees % 180 == 0;
		if (!landscape) {
			int temp = width;
			//noinspection SuspiciousNameCombination we need them when orientation is portrait
			width = height;
			height = temp;
		}

		@SuppressWarnings("deprecation") android.hardware.Camera.Size previewSize =
				AndroidTools.getOptimalSize(cameraHolder.params.getSupportedPreviewSizes(), width, height);
		@SuppressWarnings("deprecation") android.hardware.Camera.Size pictureSize =
				AndroidTools.getOptimalSize(cameraHolder.params.getSupportedPictureSizes(), width, height);
		LOG.debug("orient: {}, size: {}x{} ({}), surface: {}x{} ({}), preview: {}x{} ({}), picture: {}x{} ({})", //
				degrees, //
				width, height, (float)width / (float)height, //
				w, h, (float)w / (float)h, //
				previewSize.width, previewSize.height, (float)previewSize.width / (float)previewSize.height, //
				pictureSize.width, pictureSize.height, (float)pictureSize.width / (float)pictureSize.height //
		);
		cameraHolder.params.setPreviewSize(previewSize.width, previewSize.height);
		cameraHolder.params.setPictureSize(pictureSize.width, pictureSize.height);
		cameraHolder.params.setRotation(degrees);
		cameraHolder.params.set("orientation", landscape? "landscape" : "portrait");
		cameraHolder.camera.setParameters(cameraHolder.params);
		cameraHolder.camera.setDisplayOrientation(degrees);
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
				listener.onResume(this);
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
				listener.onPause(this);
			}
		} catch (RuntimeException ex) {
			LOG.warn("ignore: tried to stop a non-existent preview", ex);
		}
	}

	private void started(CameraHolder holder) {
		cameraHolder = holder;
		missedEvents.replay();
		listener.onCreate(this);
	}

	private void finished() {
		cameraHolder = null;
		listener.onDestroy(this);
	}

	/**
	 * Initiate taking a picture, if {@code autoFocus} is {@code false} the picture will be taken immediately;
	 * otherwise an auto-focus will be triggered if possible.
	 * {@link CameraPictureListener#onFocus(boolean)} will be only called if auto-focus was requested.
	 * Taking the picture can be cancelled by {@link CameraPictureListener#onFocus(boolean) onFocus}.
	 */
	@SuppressWarnings("deprecation")
	public void takePicture(final CameraPictureListener callback, boolean autoFocus) {
		LOG.trace("{} Taking picture", cameraHolder);
		if (cameraHolder == null) {
			return;
		}
		if (autoFocus) {
			focus(callback);
			return;
		}
		cameraHolder.camera.takePicture(new android.hardware.Camera.ShutterCallback() {
			@Override public void onShutter() {
				post(new Runnable() {
					@Override public void run() {
						listener.onShutter(CameraPreview.this);
					}
				});
				post(new Runnable() {
					@Override public void run() {
						listener.onPause(CameraPreview.this);
					}
				});
			}
		}, null, null, new android.hardware.Camera.PictureCallback() {
			@Override public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
				callback.onTaken(data);
			}
		});
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
	@SuppressWarnings("deprecation")
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
			cameraHolder.camera.autoFocus(cameraCallback);
		} else {
			cameraCallback.onAutoFocus(true, cameraHolder.camera);
		}
	}

	public void setFlash(boolean flash) {
		if (cameraHolder == null) {
			return;
		}
		@SuppressWarnings("deprecation")
		String flashMode = flash
				? android.hardware.Camera.Parameters.FLASH_MODE_ON
				: android.hardware.Camera.Parameters.FLASH_MODE_OFF;
		cameraHolder.params.setFlashMode(flashMode);
		cameraHolder.camera.setParameters(cameraHolder.params);
	}

	@SuppressWarnings("deprecation")
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

		@Override public String toString() {
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
		private final Handler mHandler;

		@MainThread CameraHandlerThread() {
			super("CameraHandlerThread");
			start();
			mHandler = new Handler(getLooper());
		}

		void startOpenCamera() {
			mHandler.post(new Runnable() {
				@SuppressLint("WrongThread")
				@WorkerThread
				@Override public void run() {
					try {
						final CameraHolder holder = new CameraHolder(findCamera());
						//noinspection ResourceType post should be safe to call from background
						CameraPreview.this.post(new Runnable() {
							@UiThread
							public void run() {
								CameraPreview.this.started(holder);
							}
						});
					} catch (final RuntimeException ex) {
						LOG.error("Error setting up camera", ex);
						//noinspection ResourceType post should be safe to call from background
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
					return 0; // TODO front camera?
				}
			});
		}

		void stopThread() {
			getLooper().quit();
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
}
