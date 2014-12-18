package net.twisterrob.android.utils.tools;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.*;
import android.os.Build.*;
import android.preference.ListPreference;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.support.v4.view.*;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import static android.util.TypedValue.*;

public abstract class AndroidTools {
	private static final Logger LOG = LoggerFactory.getLogger(AndroidTools.class);

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
		return TypedValue.applyDimension(COMPLEX_UNIT_DIP, number, context.getResources().getDisplayMetrics());
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
		for (String key : new TreeSet<String>(bundle.keySet())) {
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

	@SuppressWarnings("deprecation")
	public static Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
		if (sizes == null) {
			return null;
		}

		Camera.Size optimalSize = findClosestAspect(sizes, w, h, 0.1);

		if (optimalSize == null) {
			optimalSize = findClosestAspect(sizes, w, h, Double.POSITIVE_INFINITY);
		}

		return optimalSize;
	}

	@SuppressWarnings("deprecation")
	private static Camera.Size findClosestAspect(List<Camera.Size> sizes, int width, int height, double tolerance) {
		Camera.Size optimalSize = null;

		final double targetRatio = (double)width / (double)height;
		double minDiff = Double.MAX_VALUE;
		for (Camera.Size size : sizes) {
			double ratio = (double)size.width / (double)size.height;
			if (Math.abs(ratio - targetRatio) <= tolerance && Math.abs(size.height - height) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - height);
			}
		}
		return optimalSize;
	}

	public static boolean hasCameraHardware(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	@SuppressWarnings("deprecation")
	public static int calculateRotation(Context context, Camera.CameraInfo cameraInfo) {
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
	@SafeVarargs
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

	/**
	 * Tries to find an instance of {@code eventsClass} among the {@code fragment}'s parents,
	 * that is {@link Fragment#getParentFragment()} and {@link Fragment#getActivity()}.
	 * Closest one wins, activity being the farthest.
	 *
	 * @throws IllegalArgumentException if callback is null or is not the right {@code eventsClass}.
	 */
	public static @NonNull <T> T findAttachedListener(@NonNull Fragment fragment, @NonNull Class<T> eventsClass)
			throws IllegalArgumentException {
		T listener = null;
		List<Fragment> parents = getParents(fragment);
		Iterator<Fragment> iterator = parents.iterator();
		while (listener == null && iterator.hasNext()) {
			listener = tryGetAttachedListener(iterator.next(), eventsClass);
		}
		if (listener == null) {
			listener = tryGetAttachedListener(fragment.getActivity(), eventsClass);
		}
		if (listener != null) {
			return listener;
		} else {
			throw new IllegalArgumentException("One of " + fragment + "'s parents (" + parents + ") or its activity ("
					+ fragment.getActivity() + ") must implement " + eventsClass);
		}
	}

	public static @NonNull List<Fragment> getParents(@NonNull Fragment fragment) {
		List<Fragment> parents = new LinkedList<>();
		Fragment parent = fragment.getParentFragment();
		while (parent != null) {
			parents.add(parent);
			parent = parent.getParentFragment();
		}
		return parents;
	}

	public static @Nullable <T> T tryGetAttachedListener(Object callback, @NonNull Class<T> eventsClass) {
		if (eventsClass.isInstance(callback)) {
			return eventsClass.cast(callback);
		} else {
			return null;
		}
	}

	/** @throws IllegalArgumentException if callback is null or is not the right {@code eventsClass}. */
	public static @NonNull <T> T getAttachedListener(Object callback, @NonNull Class<T> eventsClass)
			throws IllegalArgumentException {
		T listener = tryGetAttachedListener(callback, eventsClass);
		if (listener != null) {
			return listener;
		} else {
			throw new IllegalArgumentException("Parent " + callback + " must implement " + eventsClass);
		}
	}

	public static View prepareSearch(Activity activity, Menu menu, int searchItemID) {
		SearchManager searchManager = (SearchManager)activity.getSystemService(Context.SEARCH_SERVICE);
		MenuItem item = menu.findItem(searchItemID);
		if (item == null) {
			return null;
		}
		View view = MenuItemCompat.getActionView(item);
		if (view instanceof android.support.v7.widget.SearchView) {
			android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView)view;
			SearchableInfo info = searchManager.getSearchableInfo(activity.getComponentName());
			if (info == null) {
				throw new NullPointerException("No searchable info for " + activity.getComponentName()
						+ "\nDid you define <meta-data android:name=\"android.app.default_searchable\" android:value=\".SearchActivity\" />"
						+ "\neither on application level or inside the activity in AndroidManifest.xml?"
						+ "\nAlso make sure that in the merged manifest the class name resolves correctly (package).");
			}
			searchView.setSearchableInfo(info);
			return searchView;
		} else {
			SearchViewCompat.setSearchableInfo(view, activity.getComponentName());
			return view;
		}
	}

	public static void screenshot(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(bitmap));
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		try {
			File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			storageDir.mkdirs();
			File file = File.createTempFile(timeStamp, ".png", storageDir);
			@SuppressWarnings("resource")
			OutputStream stream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
			stream.close();
			Log.i("SCREENSHOT", "adb pull " + file);
		} catch (IOException e) {
			Log.e("SCREENSHOT", "Cannot save screenshot of " + view, e);
		}
	}

	public static void setEnabled(MenuItem item, boolean enabled) {
		item.setEnabled(enabled);
		Drawable icon = item.getIcon();
		if (icon != null) {
			icon = icon.mutate();
			icon.setAlpha(enabled? 0xFF : 0x80);
			item.setIcon(icon);
		}
	}

	/** Call from {@link android.app.Activity#onMenuOpened(int, Menu)}. */
	public static void showActionBarOverflowIcons(int featureId, Menu menu, boolean show) {
		// http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
		if ((featureId == WindowCompat.FEATURE_ACTION_BAR || featureId == WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
				&& menu != null && "MenuBuilder".equals(menu.getClass().getSimpleName())) {
			try {
				Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
				m.setAccessible(true);
				m.invoke(menu, show);
			} catch (NoSuchMethodException e) {
				LOG.error("ActionBar overflow icons hack failed", e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static void setItemChecked(AdapterView parent, int position, boolean value) {
		if (parent instanceof ListView) {
			((ListView)parent).setItemChecked(position, value);
		} else if (parent instanceof AbsListView) {
			if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
				((AbsListView)parent).setItemChecked(position, value);
			}
		} else {
			LOG.warn("Cannot setItemChecked({}) #{} on {}", value, position, parent);
		}
	}
}
