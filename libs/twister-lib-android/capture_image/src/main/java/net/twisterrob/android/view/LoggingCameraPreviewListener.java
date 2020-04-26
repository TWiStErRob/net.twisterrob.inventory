package net.twisterrob.android.view;

import org.slf4j.*;

public class LoggingCameraPreviewListener implements CameraPreview.CameraPreviewListener {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingCameraPreviewListener.class);

	@Override public void onCreate(CameraPreview preview) {
		LOG.trace("onCreate({})", preview);
	}

	@Override public void onResume(CameraPreview preview) {
		LOG.trace("onResume({})", preview);
	}

	@Override public void onShutter(CameraPreview preview) {
		LOG.trace("onShutter({})", preview);
	}

	@Override public void onPause(CameraPreview preview) {
		LOG.trace("onPause({})", preview);
	}

	@Override public void onDestroy(CameraPreview preview) {
		LOG.trace("onDestroy({})", preview);
	}
}
