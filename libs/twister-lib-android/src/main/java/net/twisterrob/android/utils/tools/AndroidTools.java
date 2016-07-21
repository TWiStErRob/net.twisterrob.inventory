package net.twisterrob.android.utils.tools;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.*;

import org.slf4j.*;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.*;
import android.os.Build.*;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.preference.ListPreference;
import android.support.annotation.*;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.*;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import static android.util.TypedValue.*;

import net.twisterrob.android.annotation.*;
import net.twisterrob.android.utils.tostring.stringers.AndroidStringerRepo;
import net.twisterrob.android.utils.tostring.stringers.name.*;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.*;
import net.twisterrob.java.utils.tostring.*;
import net.twisterrob.java.utils.tostring.stringers.DefaultNameStringer;

@SuppressWarnings("unused")
public /*static*/ abstract class AndroidTools {
	private static final Logger LOG = LoggerFactory.getLogger(AndroidTools.class);

	private static Context appContext;
	public static synchronized void setContext(Context context) {
		if (appContext != null || context == null) {
			throw new IllegalArgumentException("Cannot set context twice or to null");
		}
		appContext = context.getApplicationContext();
		AndroidStringerRepo.init(StringerRepo.INSTANCE, appContext);
	}
	public static Context getContext() {
		return appContext;
	}

	private static final float CIRCLE_LIMIT = 359.9999f;
	private static final int INVALID_POSITION = -1;

	public static final @AnyRes int INVALID_RESOURCE_ID = 0;
	public static final String NULL = StringTools.NULL_STRING;
	public static final String ERROR = "error";

	public static final String ANDROID_PACKAGE = "android";
	public static final String RES_TYPE_ID = "id";
	public static final String RES_TYPE_STRING = "string";
	public static final String RES_TYPE_RAW = "raw";
	public static final String RES_TYPE_DRAWABLE = "drawable";

	public static boolean hasPermission(Context context, String permission) {
		PackageManager packageManager = context.getPackageManager();
		// alternative: context.checkCallingOrSelfPermission
		int permissionResult = packageManager.checkPermission(permission, context.getPackageName());
		return permissionResult == PackageManager.PERMISSION_GRANTED;
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

	public static float dip(Context context, float number) {
		return TypedValue.applyDimension(COMPLEX_UNIT_DIP, number, context.getResources().getDisplayMetrics());
	}
	public static int dipInt(Context context, float number) {
		return (int)dip(context, number);
	}

	public static float sp(Context context, float number) {
		return TypedValue.applyDimension(COMPLEX_UNIT_SP, number, context.getResources().getDisplayMetrics());
	}
	public static int spInt(Context context, float number) {
		return (int)sp(context, number);
	}
	
	public static @RawRes int getRawResourceID(@Nullable Context context, @NonNull String rawResourceName) {
		return getResourceID(context, RES_TYPE_RAW, rawResourceName);
	}

	public static @DrawableRes int getDrawableResourceID(@Nullable Context context, String drawableResourceName) {
		return getResourceID(context, RES_TYPE_DRAWABLE, drawableResourceName);
	}

	public static @StringRes int getStringResourceID(@Nullable Context context, @NonNull String stringResourceName) {
		return getResourceID(context, RES_TYPE_STRING, stringResourceName);
	}

	public static @NonNull CharSequence getText(@NonNull Context context, @NonNull String stringResourceName) {
		int id = getStringResourceID(context, stringResourceName);
		if (id == INVALID_RESOURCE_ID) {
			throw new NotFoundException(String.format(Locale.ROOT, "Resource '%s' is not a valid string in '%s'",
					stringResourceName, context.getPackageName()));
		}
		try {
			return context.getText(id);
		} catch (NotFoundException ex) {
			//noinspection UnnecessaryInitCause NotFoundException(String, ex) was added in API 24
			throw (NotFoundException)new NotFoundException(
					String.format(Locale.ROOT, "Resource '%s' is not a valid string in '%s'",
							stringResourceName, context.getPackageName())
			).initCause(ex);
		}
	}

	public static void setHint(EditText edit, @StringRes int resourceID) {
		ViewParent parent = edit.getParent();
		if (parent instanceof TextInputLayout) {
			((TextInputLayout)parent).setHint(edit.getResources().getText(resourceID));
		} else {
			edit.setHint(resourceID);
		}
	}
	public static void setHint(EditText edit, CharSequence text) {
		ViewParent parent = edit.getParent();
		if (parent instanceof TextInputLayout) {
			((TextInputLayout)parent).setHint(text);
		} else {
			edit.setHint(text);
		}
	}

	private static @AnyRes int getResourceID(@Nullable Context context,
			@NonNull String resourceType, @NonNull String resourceName) {
		int resID = INVALID_RESOURCE_ID;
		if (context != null) {
			resID = context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
		}
		if (resID == INVALID_RESOURCE_ID) {
			LOG.warn("No {} resource found with name '{}' in package '{}'",
					resourceType, resourceName, context != null? context.getPackageName() : null);
		}
		return resID;
	}

	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static @Nullable View findActionBarTitle(@NonNull View root) {
		return findActionBarItem(root, "action_bar_title", "mTitleTextView");
	}
	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static @Nullable View findActionBarSubTitle(@NonNull View root) {
		return findActionBarItem(root, "action_bar_subtitle", "mSubtitleTextView");
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

	@DebugHelper
	public static @NonNull <T> String toShortString(T value) {
		return new ToStringer(StringerRepo.INSTANCE, value, false).toString();
	}

	@DebugHelper
	public static @NonNull <T> String toString(T value) {
		return new ToStringer(StringerRepo.INSTANCE, value, true).toString();
	}

	@SuppressWarnings("deprecation")
	public static @NonNull android.hardware.Camera.Size getOptimalSize(
			@NonNull Collection<android.hardware.Camera.Size> sizes, int w, int h) {
		//noinspection ConstantConditions it's still possible the call the method with null
		if (sizes == null || sizes.isEmpty()) {
			throw new IllegalArgumentException("There must be at least one size to choose from.");
		}
		ArrayList<android.hardware.Camera.Size> sorted = new ArrayList<>(sizes);
		Collections.sort(sorted, new CameraSizeComparator(w, h));

		android.hardware.Camera.Size optimalSize = sorted.get(0);
		if (LOG.isTraceEnabled()) {
			LOG.trace("Optimal size selected is {}x{} from {}.",
					optimalSize.width, optimalSize.height, toString(sorted));
		}

		return optimalSize;
	}

	@SuppressWarnings("deprecation")
	private static StringBuilder toString(List<android.hardware.Camera.Size> sorted) {
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

	public static boolean hasCameraHardware(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
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
	@SafeVarargs
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static <Params> void executeParallel(
			final AsyncTask<Params, ?, ?> task, boolean force, final Params... params) {
		if (force && VERSION.SDK_INT < VERSION_CODES.DONUT) {
			throw new IllegalStateException("Cannot execute AsyncTask in parallel before DONUT");
		}
		if (!isOnUIThread()) {
			// execute/executeOnExecutor: This method must be invoked on the UI thread.
			// This is required because onPreExecute is called on the UI thread
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override public void run() {
					executePreferParallel(task, params);
				}
			});
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
	@SafeVarargs
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static <Params> void executeSerial(
			final AsyncTask<Params, ?, ?> task, boolean force, final Params... params) {
		if (force && VERSION_CODES.DONUT <= VERSION.SDK_INT && VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			throw new IllegalStateException("Cannot execute AsyncTask in serial between DONUT and HONEYCOMB");
		}
		if (!isOnUIThread()) {
			// execute/executeOnExecutor: This method must be invoked on the UI thread.
			// This is required because onPreExecute is called on the UI thread
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override public void run() {
					executePreferSerial(task, params);
				}
			});
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
		View view = MenuItemCompat.getActionView(item);
		if (view == null) {
			throw new NullPointerException("Cannot find actionView! Is it declared in XML and kept in proguard?");
		}
		if (view instanceof android.support.v7.widget.SearchView) {
			android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView)view;
			SearchableInfo info = searchManager.getSearchableInfo(activity.getComponentName());
			if (info == null) {
				throw new NullPointerException("No searchable info for " + activity.getComponentName()
						+ "\nDid you define <meta-data android:name=\"android.app.default_searchable\" android:value=\".SearchActivity\" />"
						+ "\neither on application level or inside the activity in AndroidManifest.xml?"
						+ "\nAlso make sure that in the merged manifest the class name resolves correctly (package)."
						+ "\nDouble check that the searchable.xml doesn't contain literal strings!"
				);
			}
			searchView.setSearchableInfo(info);
			return searchView;
		} else {
			SearchViewCompat.setSearchableInfo(view, activity.getComponentName());
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

	public static void enabledIf(View view, boolean isEnabled) {
		if (view != null) {
			view.setEnabled(isEnabled);
		}
	}
	public static void enabledIf(Menu menu, @IdRes int itemID, boolean isEnabled) {
		enabledIf(menu.findItem(itemID), isEnabled);
	}
	public static void enabledIf(MenuItem item, boolean isEnabled) {
		if (item == null) {
			return;
		}
		item.setEnabled(isEnabled);
		Drawable icon = item.getIcon();
		if (icon != null) {
			icon = icon.mutate();
			icon.setAlpha(isEnabled? 0xFF : 0x80);
			item.setIcon(icon);
		}
	}

	/** Borrowing from CSS terminology: <code>display:block/none</code> */
	public static void displayedIf(View view, boolean isVisible) {
		if (view != null) {
			view.setVisibility(isVisible? View.VISIBLE : View.GONE);
		}
	}
	/** Borrowing from CSS terminology: <code>visibility:visible/hidden</code> */
	public static void visibleIf(View view, boolean isVisible) {
		if (view != null) {
			view.setVisibility(isVisible? View.VISIBLE : View.INVISIBLE);
		}
	}
	public static void visibleIf(Menu menu, @IdRes int itemID, boolean isVisible) {
		visibleIf(menu.findItem(itemID), isVisible);
	}
	public static void visibleIf(MenuItem item, boolean isVisible) {
		if (item != null) {
			item.setVisible(isVisible);
		}
	}
	public static void displayedIfHasText(TextView view) {
		if (view == null) {
			return;
		}
		CharSequence text = view.getText();
		displayedIf(view, !TextUtils.isEmpty(text) && 0 < TextUtils.getTrimmedLength(text));
	}
	public static void visibleIfHasText(TextView view) {
		if (view == null) {
			return;
		}
		CharSequence text = view.getText();
		visibleIf(view, text != null && 0 < text.length());
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static void updateStartMargin(View view, int margin) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.leftMargin = margin;
		} else {
			params.setMarginStart(margin);
		}
		view.setLayoutParams(params);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static void updateEndMargin(View view, int margin) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.rightMargin = margin;
		} else {
			params.setMarginEnd(margin);
		}
		view.setLayoutParams(params);
	}

	public static void updateHeight(View view, int height) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = height;
		view.setLayoutParams(params);
	}

	public static void updateWidth(View view, int width) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = width;
		view.setLayoutParams(params);
	}

	public static void updateWidthAndHeight(View view, int width, int height) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = width;
		params.height = height;
		view.setLayoutParams(params);
	}

	@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
	// TOFIX all built-in and support LayoutParams
	public static void updateGravity(View view, int gravity) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params instanceof FrameLayout.LayoutParams) {
			((FrameLayout.LayoutParams)params).gravity = gravity;
		} else if (params instanceof LinearLayout.LayoutParams) {
			((LinearLayout.LayoutParams)params).gravity = gravity;
		} else if (params instanceof DrawerLayout.LayoutParams) {
			((DrawerLayout.LayoutParams)params).gravity = gravity;
		} else if (VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT
				&& params instanceof GridLayout.LayoutParams) {
			((GridLayout.LayoutParams)params).setGravity(gravity);
		}
		view.setLayoutParams(params);
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
				Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
				m.setAccessible(true);
				m.invoke(menu, show);
			} catch (NoSuchMethodException ex) {
				LOG.warn("ActionBar overflow icons hack failed", ex);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
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

	public static View findClosest(View view, @IdRes int viewId) {
		if (view.getId() == viewId) {
			return view;
		}
		ViewParent parent = view.getParent();
		if (parent instanceof ViewGroup) {
			ViewGroup group = (ViewGroup)parent;
			for (int index = 0; index < group.getChildCount(); index++) {
				View child = group.getChildAt(index);
				if (child.getId() == viewId) {
					return child;
				}
			}
			return findClosest(group, viewId);
		}
		return null;
	}

	/** @see ComponentCallbacks2 */
	@DebugHelper
	public static String toTrimMemoryString(@TrimMemoryLevel int level) {
		return TrimMemoryLevel.Converter.toString(level);
	}

	/** @see Intent#FLAG_* */
	@SuppressWarnings("JavadocReference")
	@DebugHelper
	public static String toActivityIntentFlagString(@IntentFlags int flags) {
		return IntentFlags.Converter.toString(flags, true);
	}

	public static String toDrawerLayoutStateString(int state) {
		switch (state) {
			case DrawerLayout.STATE_IDLE:
				return "STATE_IDLE";
			case DrawerLayout.STATE_DRAGGING:
				return "STATE_DRAGGING";
			case DrawerLayout.STATE_SETTLING:
				return "STATE_SETTLING";
			default:
				return "state::" + state;
		}
	}

	public static String toColorString(int color) {
		return String.format(Locale.ROOT, "#%02X%02X%02X%02X",
				Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
	}

	@DebugHelper
	public static String toFeatureString(@WindowFeature int featureId) {
		return WindowFeature.Converter.toString(featureId);
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

	/** @see ContentProvider#openPipeHelper */
	public static ParcelFileDescriptor stream(final byte... contents) throws FileNotFoundException {
		ParcelFileDescriptor[] pipe;
		try {
			pipe = ParcelFileDescriptor.createPipe();
		} catch (IOException e) {
			throw new FileNotFoundException(e.toString());
		}
		final ParcelFileDescriptor readEnd = pipe[0];
		final ParcelFileDescriptor writeEnd = pipe[1];

		Runnable startStreaming = new Runnable() {
			@Override public void run() {
				executePreferParallel(new AsyncTask<Object, Object, Object>() {
					@Override protected Object doInBackground(Object... params) {
						InputStream in = new ByteArrayInputStream(contents);
						OutputStream out = new AutoCloseOutputStream(writeEnd);
						try {
							IOTools.copyStream(in, out);
						} catch (IOException e) {
							IOTools.ignorantCloseWithError(writeEnd, e.toString());
						}
						try {
							writeEnd.close();
						} catch (IOException e) {
							LOG.warn("Failure closing pipe", e);
						}
						return null;
					}
				});
			}
		};

		if (isOnUIThread()) {
			startStreaming.run();
		} else {
			new Handler(Looper.getMainLooper()).post(startStreaming);
		}
		return readEnd;
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
	@DebugHelper
	public static String toNameString(Context context, @IdRes int id) {
		return new ToStringer(StringerRepo.INSTANCE, id, ResourceNameStringer.INSTANCE).toString();
	}
	@DebugHelper
	public static String toNameString(Fragment fragment) {
		return new ToStringer(StringerRepo.INSTANCE, fragment, FragmentNameStringer.INSTANCE).toString();
	}
	@DebugHelper
	public static String toNameString(Activity activity) {
		return new ToStringer(StringerRepo.INSTANCE, activity, DefaultNameStringer.INSTANCE).toString();
	}
	@DebugHelper
	public static String toNameString(Object object) {
		return new ToStringer(StringerRepo.INSTANCE, object, DefaultNameStringer.INSTANCE).toString();
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
	@TargetApi(VERSION_CODES.JELLY_BEAN)
	public static void setBackground(View view, Drawable backgroundDrawable) {
		if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
			view.setBackground(backgroundDrawable);
		} else {
			view.setBackgroundDrawable(backgroundDrawable);
		}
	}

	@UiThread
	public interface PopupCallbacks<T> {
		void finished(T value);
		PopupCallbacks<?> NO_CALLBACK = new DoNothing();

		class DoNothing implements PopupCallbacks<Object> {
			@Override public void finished(Object value) {

			}
			@SuppressWarnings("unchecked")
			public static <T> PopupCallbacks<T> instance() {
				return (PopupCallbacks<T>)NO_CALLBACK;
			}
		}
	}

	@UiThread
	public static AlertDialog.Builder prompt(final Context context, final PopupCallbacks<String> callbacks) {
		final EditText input = new EditText(context);
		input.setSingleLine(true);
		showKeyboard(input);

		final AtomicReference<Dialog> dialog = new AtomicReference<>();
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = input.getText().toString();
					callbacks.finished(value);
					dialog.get().dismiss();
				}
				return false;
			}
		});
		return new AlertDialog.Builder(context) {
			@Override public @NonNull AlertDialog create() {
				AlertDialog createdDialog = super.create();
				if (null != dialog.getAndSet(createdDialog)) { // steal created dialog
					throw new UnsupportedOperationException("Cannot create multiple dialogs from this builder.");
				}
				return createdDialog;
			}
		}
				.setView(input)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						callbacks.finished(value);
					}
				})
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						callbacks.finished(null);
					}
				});
	}

	/**
	 * Warning: call this before attaching the view to the parent if the view is created dynamically.
	 * @see <a href="http://stackoverflow.com/a/19004929/253468">How to show soft-keyboard when EditText is focused?</a>
	 */
	@TargetApi(VERSION_CODES.HONEYCOMB_MR1)
	public static void showKeyboard(final View view) {
		InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		final Runnable tryAgain = new Runnable() {
			@Override public void run() {
				showKeyboard(view);
			}
		};

		view.clearFocus();

		if (view.isShown()) {
			view.requestFocus();
			imm.showSoftInput(view, 0);
		} else {
			if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB_MR1) {
				view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override public void onGlobalLayout() {
						view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						view.post(tryAgain);
					}
				});
				view.post(tryAgain);
			} else {
				view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
					@Override public void onViewAttachedToWindow(View view) {
						view.removeOnAttachStateChangeListener(this);
						view.post(tryAgain);
					}

					@Override public void onViewDetachedFromWindow(View view) {
						view.removeOnAttachStateChangeListener(this);
					}
				});
			}
		}
	}

	public static void hideKeyboard(View input) {
		InputMethodManager imm = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
	}

	public static AlertDialog.Builder confirm(Context context, final PopupCallbacks<Boolean> callbacks) {
		return new AlertDialog.Builder(context) {
			@Override public AlertDialog create() {
				AlertDialog dialog = super.create();
				dialog.setCanceledOnTouchOutside(true);
				return dialog;
			}
		}
				.setPositiveButton(android.R.string.yes, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						callbacks.finished(true);
					}
				})
				.setNegativeButton(android.R.string.no, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						callbacks.finished(false);
					}
				})
				.setCancelable(true)
				.setOnCancelListener(new OnCancelListener() {
					@Override public void onCancel(DialogInterface dialog) {
						callbacks.finished(null);
					}
				});
	}
	public static AlertDialog.Builder notify(Context context, final PopupCallbacks<Boolean> callbacks) {
		return new AlertDialog.Builder(context) {
			@Override public AlertDialog create() {
				AlertDialog dialog = super.create();
				dialog.setCanceledOnTouchOutside(true);
				return dialog;
			}
		}
				.setNeutralButton(android.R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						callbacks.finished(true);
					}
				})
				.setCancelable(true)
				.setOnCancelListener(new OnCancelListener() {
					@Override public void onCancel(DialogInterface dialog) {
						callbacks.finished(null);
					}
				});
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
		MediaScannerConnection.scanFile(context, new String[] {file.getPath()}, null, null);
		//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
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

	protected AndroidTools() {
		// static utility class
	}
}
