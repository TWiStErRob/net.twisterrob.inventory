package net.twisterrob.android.utils.tools;

import java.util.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.AdapterView;

public abstract class AndroidTools {
	private AndroidTools() {
		// static class
	}

	public static boolean hasPermission(Context context, String permission) {
		PackageManager packageManager = context.getPackageManager();
		// alternative: context.checkCallingOrSelfPermission
		int permissionResult = packageManager.checkPermission(permission, context.getPackageName());
		return permissionResult == PackageManager.PERMISSION_GRANTED;
	}

	public static void selectByID(AdapterView<?> view, long id) {
		CursorAdapter myAdapter = (CursorAdapter)view.getAdapter();
		for (int i = 0, n = myAdapter.getCount(); i < n; i++) {
			if (myAdapter.getItemId(i) == id) {
				view.setSelection(i);
				break;
			}
		}
	}

	public static String toString(Bundle bundle) {
		if (bundle == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Bundle of ").append(bundle.size()).append('\n');
		sb.append(new TreeSet<String>(bundle.keySet())).append('\n');
		for (String key: bundle.keySet()) {
			String value = toString(bundle.get(key));
			sb.append('\t').append(key).append("=").append(value).append('\n');
		}
		return sb.toString();
	}

	public static String toString(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof Bundle) {
			return AndroidTools.toString((Bundle)value);
		}
		String type = value.getClass().getSimpleName();
		return "(" + type + ")" + value;
	}

	public static Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
		if (sizes == null) {
			return null;
		}

		final double ASPECT_TOLERANCE = 0.1;
		final int targetHeight = h;
		final double targetRatio = (double)w / (double)h;

		Camera.Size optimalSize = null;

		if (optimalSize == null) {
			double minDiff = Double.MAX_VALUE;
			for (Camera.Size size: sizes) {
				double ratio = (double)size.width / (double)size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
					continue; // try to find the one which have very similar ratio first
				}

				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		if (optimalSize == null) {
			double minDiff = Double.MAX_VALUE;
			for (Camera.Size size: sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}

	public static boolean hasCameraHardware(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public static int calculateRotation(Context context, CameraInfo cameraInfo) {
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = windowManager.getDefaultDisplay().getRotation();
		int degrees = rotation * 90; // consider using Surface.ROTATION_ constants

		int result;
		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (cameraInfo.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (cameraInfo.orientation - degrees + 360) % 360;
		}
		return result;
	}
}
