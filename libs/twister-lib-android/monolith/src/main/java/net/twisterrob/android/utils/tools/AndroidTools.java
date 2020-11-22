package net.twisterrob.android.utils.tools;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.*;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.os.Build.*;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.preference.ListPreference;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.*;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.exceptions.StackTrace;
import net.twisterrob.java.utils.*;

import static net.twisterrob.android.AndroidConstants.*;

@SuppressWarnings({"unused", "StaticMethodOnlyUsedInOneClass", "SameParameterValue"})
public /*static*/ abstract class AndroidTools {
	private static final Logger LOG = LoggerFactory.getLogger(AndroidTools.class);

	private static final float CIRCLE_LIMIT = 359.9999f;

	public static final String NULL = StringTools.NULL_STRING;
	public static final String ERROR = "error";

	public static boolean hasPermission(Context context, String permission) {
		PackageManager packageManager = context.getPackageManager();
		// alternative: context.checkCallingOrSelfPermission
		int permissionResult = packageManager.checkPermission(permission, context.getPackageName());
		return permissionResult == PackageManager.PERMISSION_GRANTED;
	}

	public static @NonNull List<String> getDeclaredPermissions(@NonNull Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
			String[] requestedPermissions = packageInfo != null
					? packageInfo.requestedPermissions
					: null;
			return requestedPermissions != null
					? Arrays.asList(requestedPermissions)
					: Collections.<String>emptyList();
		} catch (PackageManager.NameNotFoundException e) {
			return Collections.emptyList();
		}
	}

	public static List<Intent> resolveIntents(Context context, Intent originalIntent, int flags) {
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> resolved = packageManager.queryIntentActivities(originalIntent, flags);
		List<Intent> result = new ArrayList<>(resolved.size());
		for (ResolveInfo info : resolved) {
			Intent intent = new Intent(originalIntent);
			intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
			intent.setPackage(info.activityInfo.packageName);
			result.add(intent);
		}
		return result;
	}

	public static int findItemPosition(Adapter adapter, long id) {
		for (int position = 0, n = adapter.getCount(); position < n; position++) {
			if (adapter.getItemId(position) == id) {
				return position;
			}
		}
		return INVALID_POSITION;
	}

	public static void selectByID(AdapterView<?> view, long id) {
		int position = findItemPosition(view.getAdapter(), id);
		if (position != INVALID_POSITION) {
			view.setSelection(position);
		}
	}

	public static void setHint(EditText edit, @StringRes int resourceID) {
		setHint(edit, edit.getResources().getText(resourceID));
	}
	public static void setHint(EditText edit, CharSequence text) {
		ViewParent parent = edit.getParent(); // support < 24.2.0 has direct child
		ViewParent grandParent = edit.getParent().getParent(); // 24.2.+ uses an intermediate FrameLayout
		if (grandParent instanceof TextInputLayout) {
			((TextInputLayout)grandParent).setHint(text);
		} else if (parent instanceof TextInputLayout) {
			((TextInputLayout)parent).setHint(text);
		} else {
			edit.setHint(text);
		}
	}

	public static void setError(EditText edit, @StringRes int resourceID) {
		setError(edit, edit.getResources().getText(resourceID));
	}
	public static void setError(EditText edit, CharSequence text) {
		ViewParent parent = edit.getParent(); // support < 24.2.0 has direct child
		ViewParent grandParent = edit.getParent().getParent(); // 24.2.+ uses an intermediate FrameLayout
		if (grandParent instanceof TextInputLayout) {
			TextInputLayout layout = (TextInputLayout)grandParent;
			layout.setErrorEnabled(text != null);
			layout.setError(text);
		} else if (parent instanceof TextInputLayout) {
			TextInputLayout layout = (TextInputLayout)parent;
			layout.setErrorEnabled(text != null);
			layout.setError(text);
		} else {
			edit.setError(text);
		}
	}

	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static @Nullable View findActionBarTitle(@NonNull View root) {
		return findActionBarItem(root, "action_bar_title", "mTitleTextView");
	}
	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static @Nullable View findActionBarSubTitle(@NonNull View root) {
		return findActionBarItem(root, "action_bar_subtitle", "mSubtitleTextView");
	}
	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static @Nullable View findActionBarHome(@NonNull View root) {
		return findActionBarItem(root, "NO_ID", "mNavButtonView");
	}

	private static @Nullable View findActionBarItem(@NonNull View root,
			@NonNull String resourceName, @NonNull String toolbarFieldName) {
		View result = findViewSupportOrAndroid(root, resourceName);

		if (result == null) {
			View actionBar = findViewSupportOrAndroid(root, "action_bar");
			if (actionBar != null) {
				result = ReflectionTools.get(actionBar, toolbarFieldName);
			}
		}
		if (result == null && root.getClass().getName().endsWith("Toolbar")) {
			result = ReflectionTools.get(root, toolbarFieldName);
		}
		return result;
	}

	@SuppressWarnings("ConstantConditions")
	private static @Nullable View findViewSupportOrAndroid(@NonNull View root, @NonNull String resourceName) {
		Context context = root.getContext();
		View result = null;
		if (result == null) {
			int supportID = context.getResources().getIdentifier(resourceName, RES_TYPE_ID, context.getPackageName());
			result = root.findViewById(supportID);
		}
		if (result == null) {
			int androidID = context.getResources().getIdentifier(resourceName, RES_TYPE_ID, ANDROID_PACKAGE);
			result = root.findViewById(androidID);
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public static @NonNull android.hardware.Camera.Size getOptimalSize(
			@NonNull Collection<android.hardware.Camera.Size> sizes, int w, int h) {
		//noinspection ConstantConditions it's still possible the call the method with null
		if (sizes == null || sizes.isEmpty()) {
			throw new IllegalArgumentException("There must be at least one size to choose from.");
		}
		List<android.hardware.Camera.Size> sorted = new ArrayList<>(sizes);
		Collections.sort(sorted, new CameraSizeComparator(w, h));

		android.hardware.Camera.Size optimalSize = sorted.get(0);
		LOG.trace("Optimal size selected is {}x{} from {}.",
				optimalSize.width, optimalSize.height, toString(sorted));

		return optimalSize;
	}

	@SuppressWarnings("deprecation")
	private static StringBuilder toString(Iterable<android.hardware.Camera.Size> sorted) {
		StringBuilder sizesString = new StringBuilder();
		sizesString.append('[');
		for (android.hardware.Camera.Size size : sorted) {
			if (sizesString.length() > 1) {
				sizesString.append(", ");
			}
			sizesString.append(size.width).append('x').append(size.height);
		}
		sizesString.append(']');
		return sizesString;
	}

	@SuppressLint("UnsupportedChromeOsCameraSystemFeature") // False positive: _ANY is 17+ and _FRONT is considered.
	public static boolean hasCameraHardware(Context context) {
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
		} else {
			return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) 
					|| context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
		}
	}

	/**
	 * @see android.view.Display#getOrientation()
	 * @see android.view.Display#getRotation()
	 * @param displayOrientation one of {@code Surface.ROTATION_d} constants
	 * @return The {@code d} in the constant name as an integer
	 */
	private static int orientationToDegrees(/*@Surface.Rotation*/ int displayOrientation) {
		//return displayOrientation * 90;
		switch (displayOrientation) {
			case Surface.ROTATION_0:
				return 0;
			case Surface.ROTATION_90:
				return 90;
			case Surface.ROTATION_180:
				return 180;
			case Surface.ROTATION_270:
				return 270;
			default:
				throw new IllegalArgumentException("Display orientation " + displayOrientation + " is not recognized.");
		}
	}

	/**
	 * @see android.hardware.Camera#setDisplayOrientation(int)
	 * @return the display's orientation in 90-increment degrees (0, 90, 180, 270)
	 */
	@SuppressWarnings("deprecation")
	public static int calculateDisplayOrientation(Context context, android.hardware.Camera.CameraInfo cameraInfo) {
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int displayOrientation = windowManager.getDefaultDisplay().getRotation();
		int degrees = orientationToDegrees(displayOrientation);

		int result;
		if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (cameraInfo.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (cameraInfo.orientation - degrees + 360) % 360;
		}
		return result;
	}

	/**
	 * @see android.hardware.Camera.Parameters#setRotation(int)
	 * @return the camera rotation to use in 90-increment degrees (0, 90, 180, 270)
	 */
	@SuppressWarnings("deprecation")
	public static int calculateRotation(Context context, android.hardware.Camera.CameraInfo cameraInfo) {
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int displayOrientation = windowManager.getDefaultDisplay().getRotation();
		int degrees = orientationToDegrees(displayOrientation);

		int result;
		if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (cameraInfo.orientation + degrees) % 360;
		} else { // back-facing
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
			// Path currently at (float)(cx + rOut * cos(end)), (float)(cy + rOut * sin(end))
			segmentPath.lineTo((float)(cx + rInn * cos(end)), (float)(cy + rInn * sin(end)));
			segmentPath.arcTo(innerRect, startAngle + sweepAngle, -sweepAngle); // drawn backwards
		}
		if (fill != null) {
			canvas.drawPath(segmentPath, fill);
		}
		if (stroke != null && fill != stroke) {
			canvas.drawPath(segmentPath, stroke);
		}
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
	 * @param cx horizontal middle point of the oval
	 * @param cy vertical middle point of the oval
	 * @param rInn inner radius of the arc segment
	 * @param rOut outer radius of the arc segment
	 * @param startAngle see {@link Canvas#drawArc}
	 * @param sweepAngle see {@link Canvas#drawArc}, capped at &plusmn;360
	 * @param fill filling paint, can be <code>null</code>
	 * @param strokeInner stroke paint for inner ring segment, can be <code>null</code>
	 * @param strokeOuter stroke paint for outer ring segment, can be <code>null</code>
	 * @param strokeSides stroke paint for lines connecting the ends of the ring segments, can be <code>null</code>
	 * @see Canvas#drawArc
	 */
	public static void drawArcSegment(Canvas canvas, float cx, float cy, float rInn, float rOut, float startAngle,
			float sweepAngle, Paint fill, Paint strokeInner, Paint strokeOuter, Paint strokeSides) {
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

		if (fill != null || strokeSides != null) { // to prevent calculating this lot of floats
			double start = toRadians(startAngle);
			double end = toRadians(startAngle + sweepAngle);
			float innerStartX = (float)(cx + rInn * cos(start));
			float innerStartY = (float)(cy + rInn * sin(start));
			float innerEndX = (float)(cx + rInn * cos(end));
			float innerEndY = (float)(cy + rInn * sin(end));
			float outerStartX = (float)(cx + rOut * cos(start));
			float outerStartY = (float)(cy + rOut * sin(start));
			float outerEndX = (float)(cx + rOut * cos(end));
			float outerEndY = (float)(cy + rOut * sin(end));
			if (fill != null) {
				Path segmentPath = new Path();
				segmentPath.moveTo(innerStartX, innerStartY);
				segmentPath.lineTo(outerStartX, outerStartY);
				segmentPath.arcTo(outerRect, startAngle, sweepAngle);
				// Path currently at outerEndX,outerEndY
				segmentPath.lineTo(innerEndX, innerEndY);
				segmentPath.arcTo(innerRect, startAngle + sweepAngle, -sweepAngle); // drawn backwards
				canvas.drawPath(segmentPath, fill);
			}
			if (!circle && strokeSides != null) {
				canvas.drawLine(innerStartX, innerStartY, outerStartX, outerStartY, strokeSides);
				canvas.drawLine(innerEndX, innerEndY, outerEndX, outerEndY, strokeSides);
			}
		}
		if (strokeInner != null) {
			canvas.drawArc(innerRect, startAngle, sweepAngle, false, strokeInner);
		}
		if (strokeOuter != null) {
			canvas.drawArc(outerRect, startAngle, sweepAngle, false, strokeOuter);
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
	 * Try to execute in parallel if the API level allows.
	 * @see #executeParallel(AsyncTask, boolean, Object[])
	 */
	@SafeVarargs
	public static <Params> void executePreferParallel(final AsyncTask<Params, ?, ?> task, final Params... params) {
		executeParallel(task, false, params);
	}

	/**
	 * @param force Force execution to be parallel.
	 *              It will not work before {@link VERSION_CODES#DONUT}, because it was not possible back then.
	 * @see AsyncTask#execute(Object[])
	 * @see <a href="http://commonsware.com/blog/2012/04/20/asynctask-threading-regression-confirmed.html">AsyncTask Threading Regression Confirmed</a>
	 * @see <a href="https://groups.google.com/forum/#!topic/android-developers/8M0RTFfO7-M">AsyncTask in Android 4.0</a>
	 * @see <a href="http://www.jayway.com/2012/11/28/is-androids-asynctask-executing-tasks-serially-or-concurrently/">AsyncTask ordering</a>
	 */
	@SuppressLint("ObsoleteSdkInt") // for historical documentation purposes
	@SafeVarargs
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static <Params> void executeParallel(
			final AsyncTask<Params, ?, ?> task, boolean force, final Params... params) {
		if (force && VERSION.SDK_INT < VERSION_CODES.DONUT) {
			throw new IllegalStateException("Cannot execute AsyncTask in parallel before DONUT");
		}
		if (!isOnUIThread()) {
			// execute/executeOnExecutor: This method must be invoked on the UI thread.
			class DelegateToUIThread implements Runnable {
				@Override public void run() {
					executePreferParallel(task, params);
				}
			}
			// This is required because onPreExecute is called on the UI thread
			new Handler(Looper.getMainLooper()).post(new DelegateToUIThread());
			return;
		}
		if (VERSION.SDK_INT < VERSION_CODES.DONUT) { // (0, 4)
			task.execute(params); // default is serial, but not forced, so let's do it
		} else if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) { // [4, 11)
			task.execute(params); // default is pooling, cannot be explicit
		} else { // [11, ∞) android commit: 81de61bfddceba0eb77b3aacea317594b0f1de49
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params); // default is serial, explicit pooling
		}
	}

	/**
	 * Try to execute in serial if the API level allows.
	 * @see #executeSerial(AsyncTask, boolean, Object[])
	 */
	@SafeVarargs
	public static <Params> void executePreferSerial(final AsyncTask<Params, ?, ?> task, final Params... params) {
		executeSerial(task, false, params);
	}

	/**
	 * @param force Force execution to be serial.
	 *              It will not work between {@link VERSION_CODES#DONUT} and {@link VERSION_CODES#HONEYCOMB}, they made
	 *              a breaking change in {@link VERSION_CODES#DONUT} with no way to get back the old serial behavior.
	 * @see AsyncTask#execute(Object[])
	 * @see #executeParallel(AsyncTask, boolean, Object[])
	 */
	@SuppressLint("ObsoleteSdkInt") // for historical documentation purposes
	@SafeVarargs
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static <Params> void executeSerial(
			final AsyncTask<Params, ?, ?> task, boolean force, final Params... params) {
		if (force && VERSION_CODES.DONUT <= VERSION.SDK_INT && VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			throw new IllegalStateException("Cannot execute AsyncTask in serial between DONUT and HONEYCOMB");
		}
		if (!isOnUIThread()) {
			// execute/executeOnExecutor: This method must be invoked on the UI thread.
			class DelegateToUIThread implements Runnable {
				@Override public void run() {
					executePreferSerial(task, params);
				}
			}
			// This is required because onPreExecute is called on the UI thread
			new Handler(Looper.getMainLooper()).post(new DelegateToUIThread());
			return;
		}
		if (VERSION.SDK_INT < VERSION_CODES.DONUT) { // (0, 4) 
			task.execute(params); // default is serial
		} else if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) { // [4, 11)
			task.execute(params); // default is pooling, but not forced, so let's do it
		} else { // [11, ∞) android commit: 81de61bfddceba0eb77b3aacea317594b0f1de49
			task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, params); // default is serial, explicit serial
		}
	}

	public static int findIndexInResourceArray(Context context, @ArrayRes int arrayResourceID, String value) {
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
			listener = tryGetAttachedListener(fragment.getContext(), eventsClass);
		}
		if (listener == null) {
			listener = tryGetAttachedListener(fragment.getActivity(), eventsClass);
		}
		if (listener == null) {
			listener = tryGetAttachedListener(fragment.getHost(), eventsClass);
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

	public static @NonNull View prepareSearch(Activity activity, Menu menu, int searchItemID) {
		SearchManager searchManager = (SearchManager)activity.getSystemService(Context.SEARCH_SERVICE);
		MenuItem item = menu.findItem(searchItemID);
		if (item == null) {
			throw new NullPointerException("Cannot find search menu item! Did you inflate it into the menu?");
		}
		View view = item.getActionView();
		if (view == null) {
			throw new NullPointerException("Cannot find actionView! Is it declared in XML and kept in proguard?");
		}
		SearchableInfo info = searchManager.getSearchableInfo(activity.getComponentName());
		if (info == null) {
			throw new NullPointerException("No searchable info for " + activity.getComponentName()
					+ "\nDid you define <meta-data android:name=\"android.app.default_searchable\""
					+ /*                      */ " android:value=\".${name of search results activity}\" />"
					+ "\neither on application level or inside the activity in AndroidManifest.xml?"
					+ "\nAlso make sure that in the merged manifest the class name resolves correctly (package)."
					+ "\nDouble check that the searchable.xml doesn't contain literal strings for label and hint!"
			);
		}
		if (view instanceof androidx.appcompat.widget.SearchView) {
			androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView)view;
			searchView.setSearchableInfo(info);
			return searchView;
		} else if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			android.widget.SearchView searchView = (android.widget.SearchView)view;
			searchView.setSearchableInfo(info);
			return searchView;
		} else {
			LOG.warn("Unknown SearchView: {}, prepareSearch unsuccessful.", view, new StackTrace());
			return view;
		}
	}

	@SuppressLint("LogConditional") // Should only be used in debug code
	@DebugHelper
	public static void screenshot(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		view.draw(new Canvas(bitmap));
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
		try {
			File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			IOTools.ensure(storageDir);
			File file = File.createTempFile(timeStamp, ".png", storageDir);
			@SuppressWarnings("resource")
			OutputStream stream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
			stream.close();
			Log.d("SCREENSHOT", "adb pull " + file);
		} catch (IOException e) {
			Log.e("SCREENSHOT", "Cannot save screenshot of " + view, e);
		}
	}

	/**
	 * Call from {@link Activity#onPrepareOptionsMenu(Menu)} or from {@link Activity#onMenuOpened(int, Menu)}
	 * when {@code featureId} is {@link WindowCompat#FEATURE_ACTION_BAR}
	 * (may need the overlay constant too depending on theme).
	 *
	 * @see <a href="http://b.android.com/171440">AppCompatActivity.onMenuOpened is not called any more in 22.x</a>
	 */
	public static void showActionBarOverflowIcons(Menu menu, boolean show) {
		// http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
		if (menu != null && "MenuBuilder".equals(menu.getClass().getSimpleName())) {
			try {
				@SuppressLint("PrivateApi")
				Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
				m.setAccessible(true);
				m.invoke(menu, show);
			} catch (Throwable ex) {
				LOG.warn("ActionBar overflow icons hack failed", ex);
			}
		}
	}

	/**
	 * Similar to using the below flags in the theme, but works after the fact.
	 * <pre><code>
	 * &lt;item name="android:windowTranslucentStatus">true&lt;/item>
	 * &lt;item name="android:windowTranslucentNavigation">true&lt;/item>
	 * </code></pre>
	 * Leaves the scrim (shadow) intact.
	 * CONSIDER do something with FLAG_TRANSLUCENT_NAVIGATION as well?
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	public static void setTranslucentStatusBar(Window window) {
		if (window == null) {
			LOG.warn("No window while setting translucent status bar!", new StackTrace());
			return;
		}
		if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static void setTranslucentStatusBar(Window window, @ColorInt int lollipopColor) {
		if (window == null) {
			LOG.warn("No window while setting translucent status bar to {}!", lollipopColor, new StackTrace());
			return;
		}
		setTranslucentStatusBar(window);
		if (VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(lollipopColor);
			if (Color.alpha(lollipopColor) < 0xFF) {
				window.getDecorView()
				      .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			}
		}
	}

	/**
	 * @see <a href="http://blog.raffaeu.com/archive/2015/04/11/android-and-the-transparent-status-bar.aspx">
	 *     Make the StatusBar transparent</a>
	 */
	public static int getStatusBarHeight(Context context) {
		int result = 25; // safe bet, better bigger than nothing
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId != INVALID_RESOURCE_ID) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static void accountForStatusBar(View view) {
		boolean needsOffset = false;
		Activity activity = findActivity(view);
		if (activity != null) {
			Window window = activity.getWindow();
			int flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS // set pre-Lollipop
					| WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS; // set post-Lollipop
			needsOffset = (window.getAttributes().flags & flags) != 0;
		} else {
			LOG.warn("Cannot account for status bar, {} is not in an Activity.", view);
		}
		if (needsOffset) {
			ViewGroup.LayoutParams params = view.getLayoutParams();
			if (params instanceof MarginLayoutParams) {
				MarginLayoutParams marginParams = (MarginLayoutParams)params;
				marginParams.topMargin += AndroidTools.getStatusBarHeight(view.getContext());
				view.setLayoutParams(marginParams);
			} else if (params == null) {
				MarginLayoutParams marginParams =
						new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				marginParams.topMargin = AndroidTools.getStatusBarHeight(view.getContext());
				view.setLayoutParams(marginParams);
			} else {
				LOG.warn("Cannot account for status bar, {} doesn't have margin layout params: {}", view, params);
			}
		}
	}

	private static Activity findActivity(View view) {
		Context context = view.getContext();
		while (context != null && !(context instanceof Activity)) {
			if (context instanceof ContextWrapper) {
				context = ((ContextWrapper)context).getBaseContext();
			} else {
				context = null;
			}
		}
		return (Activity)context;
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static void setItemChecked(AdapterView<?> parent, int position, boolean value) {
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

	/** @see PackageManager#getActivityInfo(ComponentName, int) */
	public static ActivityInfo getActivityInfo(Activity activity, int flags) {
		try {
			return activity.getPackageManager().getActivityInfo(activity.getComponentName(), flags);
		} catch (NameNotFoundException e) {
			LOG.warn("Activity doesn't exists, but has an instance? {}", activity, e);
			throw new RuntimeException(e);
		}
	}

	public static ParcelFileDescriptor stream(final @NonNull byte... contents) throws FileNotFoundException {
		return stream(new ByteArrayInputStream(contents));
	}
	/** @see ContentProvider#openPipeHelper */
	public static ParcelFileDescriptor stream(final @NonNull InputStream in) throws FileNotFoundException {
		class Streamer implements OutWriter {
			@Override public void write(OutputStream out) throws IOException {
				try {
					IOTools.copyStream(in, out, false);
				} finally {
					IOTools.ignorantClose(in);
				}
			}
		}
		return stream(new Streamer());
	}
	public interface OutWriter {
		void write(OutputStream out) throws IOException;
	}
	/** @see ContentProvider#openPipeHelper */
	public static ParcelFileDescriptor stream(final OutWriter writer) throws FileNotFoundException {
		ParcelFileDescriptor[] pipe;
		try {
			pipe = ParcelFileDescriptor.createPipe();
		} catch (IOException ex) {
			throw IOTools.FileNotFoundException("Cannot create pipe", ex);
		}
		final ParcelFileDescriptor readEnd = pipe[0];
		final ParcelFileDescriptor writeEnd = pipe[1];

		class PipeCloserAsyncTask extends AsyncTask<Void, Void, Void> {
			@Override protected Void doInBackground(Void... params) {
				AutoCloseOutputStream out = new AutoCloseOutputStream(writeEnd);
				try {
					writer.write(out);
				} catch (IOException ex) {
					if (IOTools.isEPIPE(ex)) {
						LOG.warn("Receiver closed the pipe."/*, ex*/); // don't log exception,
						// because it happens quite often with Google products (GMail, Hangouts, Drive)
						IOTools.ignorantClose(writeEnd);
					} else {
						LOG.error("Streaming to pipe failed", ex);
						IOTools.ignorantCloseWithError(writeEnd, ex.toString());
					}
				}
				try {
					out.close();
				} catch (IOException ex) {
					LOG.warn("Failure closing pipe", ex);
				}
				return null;
			}
		}
		executePreferParallel(new PipeCloserAsyncTask());
		return readEnd;
	}

	/**
	 * @see <a href="http://stackoverflow.com/a/13238729/253468">Check if two Bundle objects are equal in Android?</a>
	 */
	public static boolean equals(Bundle bundle1, Bundle bundle2) {
		if (bundle1 == null || bundle2 == null) {
			return bundle1 == bundle2;
		}
		if (bundle1.size() != bundle2.size()) {
			return false;
		}

		Set<String> keys = bundle1.keySet();

		for (String key : keys) {
			Object value1 = bundle1.get(key);
			Object value2 = bundle2.get(key);
			if (value1 == null || value2 == null) {
				if (value1 != value2 || bundle1.containsKey(key) != bundle2.containsKey(key)) {
					return false;
				}
			} else if (value1.getClass().isArray() && value2.getClass().isArray()) {
				if (!ArrayTools.equals(value1, value2)) {
					return false;
				}
			} else if (value1 instanceof Bundle && value2 instanceof Bundle) {
				if (!equals((Bundle)value1, (Bundle)value2)) {
					return false;
				}
			} else {
				if (!value1.equals(value2)) {
					return false;
				}
			}
		}

		return true;
	}

	public static void unparcel(Intent intent) {
		if (intent != null) {
			unparcel(intent.getExtras());
		}
	}
	public static void unparcel(Bundle bundle) {
		if (bundle != null) {
			bundle.get(null);
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(VERSION_CODES.N)
	public static Locale getLocale(Configuration configuration) {
		if (VERSION.SDK_INT < VERSION_CODES.N) {
			return configuration.locale;
		} else {
			return configuration.getLocales().get(0);
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(VERSION_CODES.HONEYCOMB_MR2)
	public static Point getScreenSize(Display display) {
		Point point = new Point();
		if (VERSION_CODES.HONEYCOMB_MR2 <= VERSION.SDK_INT) {
			display.getSize(point);
		} else {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		return point;
	}

	public static @NonNull Point getMaximumBitmapSize(@Nullable Canvas canvas) {
		return CanvasTools.getMaximumBitmapSize(canvas);
	}

	/**
	 * Warning: call this before attaching the view to the parent if the view is created dynamically.
	 * @see <a href="http://stackoverflow.com/a/19004929/253468">How to show soft-keyboard when EditText is focused?</a>
	 */
	public static void showKeyboard(final View view) {
		InputMethodManager imm =
				(InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		class TryAgain implements Runnable {
			@Override public void run() {
				showKeyboard(view);
			}
		}

		view.clearFocus();

		if (view.isShown()) {
			view.requestFocus();
			imm.showSoftInput(view, 0);
		} else {
			view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
				@Override public void onViewAttachedToWindow(View view) {
					view.removeOnAttachStateChangeListener(this);
					view.post(new TryAgain());
				}

				@Override public void onViewDetachedFromWindow(View view) {
					view.removeOnAttachStateChangeListener(this);
				}
			});
		}
	}

	public static void hideKeyboard(View input) {
		InputMethodManager imm = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
	}

	/** Will return the first if there are more. */
	public static @NonNull ProviderInfo findProviderAuthority(
			@NonNull Context context, @NonNull Class<? extends ContentProvider> clazz) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
			for (ProviderInfo p : info.providers) {
				try {
					Class<?> providerClass = Class.forName(p.name);
					if (clazz.isAssignableFrom(providerClass)) {
						return p;
					}
				} catch (ClassNotFoundException ex) {
					LOG.warn("Cannot find provider class: {}", p.name, ex);
				}
			}
		} catch (NameNotFoundException ex) {
			LOG.error("Cannot find provider {} in {}", clazz, context, ex);
		}
		return new ProviderInfo();
	}

	public static void makeFileDiscoverable(@NonNull Context context, @NonNull File file) {
		// MediaScannerConnection doesn't unbind for some reason, so let's send an intent instead:
		// android.app.ServiceConnectionLeaked: [...] has leaked ServiceConnection
		// android.media.MediaScannerConnection@XXXXXXXX that was originally bound here
		//MediaScannerConnection.scanFile(context, new String[] {file.getPath()}, null, null);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
	}

	public static boolean isOnUIThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	public static void assertUIThread() {
		if (!isOnUIThread()) {
			throw new IllegalStateException("This should be executed on the UI thread.");
		}
	}

	public static void assertBackgroundThread() {
		if (isOnUIThread()) {
			throw new IllegalStateException("This should be executed off the UI thread.");
		}
	}

	/**
	 * Returns resource files (not res) from the classpath (i.e. from within the APK file).
	 * @param filter to filter the result,
	 *               the {@code dir} in the callback will reference the APK file itself, not a directory.
	 */
	public static List<JarEntry> findOnClasspath(FilenameFilter filter) throws IOException {
		URL manifest = AndroidTools.class.getResource("/AndroidManifest.xml"); // every app must have this
		JarURLConnection manifestConnection = (JarURLConnection)manifest.openConnection();
		JarFile apk = manifestConnection.getJarFile();
		File apkFile = new File(apk.getName());
		Enumeration<JarEntry> entries = apk.entries();
		List<JarEntry> result = new ArrayList<>();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if (filter.accept(apkFile, entry.getName())) {
				result.add(entry);
			}
		}
		return result;
	}

	protected AndroidTools() {
		// static utility class
	}
}
