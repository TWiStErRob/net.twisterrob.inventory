package net.twisterrob.android.utils.tools;

import java.util.*;

import static java.lang.Math.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.*;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.*;
import android.preference.ListPreference;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.AdapterView;

public abstract class AndroidTools {
	private static final float CIRCLE_LIMIT = 359.9999f;

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

	public static float dip(Context context, float number) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, number, context.getResources()
				.getDisplayMetrics());
	}
	public static int dipInt(Context context, float number) {
		return (int)dip(context, number);
	}

	public static int getRawResourceID(Context context, String rawResourceName) {
		return context.getResources().getIdentifier(rawResourceName, "raw", context.getPackageName());
	}

	public static int getDrawableResourceID(Context context, String drawableResourceName) {
		return context.getResources().getIdentifier(drawableResourceName, "drawable", context.getPackageName());
	}

	public static CharSequence getText(Context context, String stringResourceName) {
		int id = context.getResources().getIdentifier(stringResourceName, "string", context.getPackageName());
		return context.getText(id);
	}

	public static String toString(Bundle bundle) {
		if (bundle == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Bundle of ").append(bundle.size()).append('\n');
		for (String key: new TreeSet<String>(bundle.keySet())) {
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

	/**
	 * Draws a thick arc between the defined angles, see {@link Canvas#drawArc} for more.
	 * This method is equivalent to
	 * <pre><code>
	 * float rMid = (rInn + rOut) / 2;
	 * paint.setStyle(Style.STROKE); // there's nothing to fill
	 * paint.setStrokeWidth(rOut - rInn); // thickness
	 * canvas.drawArc(new RectF(cx - rMid, cy - rMid, cx + rMid, cy + rMid), startAngle, sweepAngle, false, paint);
	 * </code></pre>
	 * but supports different fill and stroke paints.
	 * 
	 * @param canvas
	 * @param cx horizontal middle point of the oval
	 * @param cy vertical middle point of the oval
	 * @param rInn inner radius of the arc segment
	 * @param rOut outer radius of the arc segment
	 * @param startAngle see {@link Canvas#drawArc}
	 * @param sweepAngle see {@link Canvas#drawArc}, capped at &plusmn;360
	 * @param fill filling paint, can be <code>null</code>
	 * @param stroke stroke paint, can be <code>null</code>
	 * @see Canvas#drawArc
	 */
	public static void drawArcSegment(Canvas canvas, float cx, float cy, float rInn, float rOut, float startAngle,
			float sweepAngle, Paint fill, Paint stroke) {
		boolean circle = false;
		if (sweepAngle > CIRCLE_LIMIT) {
			sweepAngle = CIRCLE_LIMIT;
			circle = true;
		}
		if (sweepAngle < -CIRCLE_LIMIT) {
			sweepAngle = -CIRCLE_LIMIT;
			circle = true;
		}

		RectF outerRect = new RectF(cx - rOut, cy - rOut, cx + rOut, cy + rOut);
		RectF innerRect = new RectF(cx - rInn, cy - rInn, cx + rInn, cy + rInn);

		Path segmentPath = new Path();
		double start = toRadians(startAngle);
		double end = toRadians(startAngle + sweepAngle);
		if (circle) {
			segmentPath.addArc(outerRect, startAngle, sweepAngle);
			segmentPath.addArc(innerRect, startAngle + sweepAngle, -sweepAngle);
		} else {
			segmentPath.moveTo((float)(cx + rInn * cos(start)), (float)(cy + rInn * sin(start)));
			segmentPath.lineTo((float)(cx + rOut * cos(start)), (float)(cy + rOut * sin(start)));
			segmentPath.arcTo(outerRect, startAngle, sweepAngle);
			segmentPath.lineTo((float)(cx + rInn * cos(end)), (float)(cy + rInn * sin(end)));
			segmentPath.arcTo(innerRect, startAngle + sweepAngle, -sweepAngle);
		}
		if (fill != null) {
			canvas.drawPath(segmentPath, fill);
		}
		if (stroke != null) {
			canvas.drawPath(segmentPath, stroke);
		}
	}

	public static void drawTextOnArc(Canvas canvas, String label, float cx, float cy, float rInn, float rOut,
			float startAngle, float sweepAngle, Paint textPaint) {
		Path midway = new Path();
		float r = (rInn + rOut) / 2;
		RectF segment = new RectF(cx - r, cy - r, cx + r, cy + r);
		midway.addArc(segment, startAngle, sweepAngle);
		canvas.drawTextOnPath(label, midway, 0, 0, textPaint);
	}

	/**
	 * @see <a href="http://www.jayway.com/2012/11/28/is-androids-asynctask-executing-tasks-serially-or-concurrently/">AsyncTask ordering</a>
	 */
	@SuppressLint("NewApi")
	public static <Params> void executeParallel(AsyncTask<Params, ?, ?> as, Params... params) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) {
			throw new IllegalStateException("Cannot execute AsyncTask parallel before DONUT (1.6 / API 4)");
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			as.execute(params);
		} else {
			as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		}
	}

	public static int findIndexInResourceArray(Context context, int arrayResourceID, String value) {
		ListPreference pref = new ListPreference(context);
		pref.setEntryValues(arrayResourceID);
		return pref.findIndexOfValue(value);
	}

	public static Intent getApplicationInfoScreen(Context context) {
		return getApplicationInfoScreen(context, context.getPackageName());
	}

	public static Intent getApplicationInfoScreen(Context context, String packageName) {
		// The specific app page
		Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.setData(Uri.parse("package:" + packageName));
		if (context.getPackageManager().resolveActivity(intent, 0) == null) {
			// The generic apps page
			intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
		}
		return intent;
	}

	public static <T> T getAttachedFragmentListener(Activity activity, Class<T> eventsClass) {
		if (eventsClass == null) {
			return null;
		}
		if (!eventsClass.isInstance(activity)) {
			throw new IllegalArgumentException("Activity " + activity.getClass().getSimpleName() + " must implement "
					+ eventsClass);
		}
		return eventsClass.cast(activity);
	}
}
