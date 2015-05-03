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
import android.content.DialogInterface.OnClickListener;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.view.*;
import android.support.v4.widget.SearchViewCompat;
import android.util.*;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import static android.util.TypedValue.*;

import net.twisterrob.android.annotation.*;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.ReflectionTools;

@SuppressWarnings("unused")
public /*static*/ abstract class AndroidTools {
	private static final Logger LOG = LoggerFactory.getLogger(AndroidTools.class);

	private static final float CIRCLE_LIMIT = 359.9999f;
	private static final int INVALID_POSITION = -1;

	public static final @AnyRes int INVALID_RESOURCE_ID = 0;
	public static final String NULL = "null";
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

	public static @RawRes int getRawResourceID(Context context, String rawResourceName) {
		return getResourceID(context, RES_TYPE_RAW, rawResourceName);
	}

	public static @DrawableRes int getDrawableResourceID(Context context, String drawableResourceName) {
		return getResourceID(context, RES_TYPE_DRAWABLE, drawableResourceName);
	}

	public static @StringRes int getStringResourceID(Context context, String stringResourceName) {
		return getResourceID(context, RES_TYPE_STRING, stringResourceName);
	}

	public static CharSequence getText(Context context, String stringResourceName) {
		int id = getStringResourceID(context, stringResourceName);
		if (id == INVALID_RESOURCE_ID) {
			throw new NotFoundException(String.format(Locale.ROOT, "Resource '%s' is not a valid string in '%s'",
					stringResourceName, context.getPackageName()));
		}
		return context.getText(id);
	}

	private static @AnyRes int getResourceID(Context context, String resourceType, String resourceName) {
		int resID = INVALID_RESOURCE_ID;
		if (context != null && resourceType != null && resourceName != null) {
			resID = context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
		}
		if (resID == INVALID_RESOURCE_ID) {
			LOG.warn("No {} resource found with name '{}' in package '{}'",
					resourceType, resourceName, context != null? context.getPackageName() : null);
		}
		return resID;
	}

	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static View findActionBarTitle(View root) {
		return findActionBarItem(root, "action_bar_title", "mTitleTextView");
	}
	/** @param root usually Activity.getWindow().getDecorView() or custom Toolbar */
	public static View findActionBarSubTitle(View root) {
		return findActionBarItem(root, "action_bar_subtitle", "mSubtitleTextView");
	}

	private static View findActionBarItem(View root, String resourceName, String toolbarFieldName) {
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
	private static View findViewSupportOrAndroid(View root, String resourceName) {
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
	public static String toLongString(Bundle bundle) {
		return toString(bundle, "Bundle of ", "\n", "\t", "\n", "");
	}

	@DebugHelper
	public static String toShortString(Bundle bundle) {
		return toString(bundle, "(Bundle)", "#{", "", ", ", "}");
	}

	@DebugHelper
	private static String toString(Bundle bundle, String number, String start, String preItem, String postItem,
			String end) {
		if (bundle == null) {
			return NULL;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(number).append(bundle.size()).append(start);
		for (Iterator<String> it = new TreeSet<>(bundle.keySet()).iterator(); it.hasNext(); ) {
			String key = it.next();
			String value = toString(bundle.get(key));

			sb.append(preItem).append(key).append("=").append(value);
			if (it.hasNext()) {
				sb.append(postItem);
			}
		}
		sb.append(end);
		return sb.toString();
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@DebugHelper
	public static String toString(Intent intent) {
		if (intent == null) {
			return NULL;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		first = append(sb, "pkg=", intent.getPackage(), "", first);
		first = append(sb, "cmp=", intent.getComponent(), "", first);

		first = append(sb, "xtra={", toShortString(intent.getExtras()), "}", first);
		if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
			first = append(sb, "clip={", intent.getClipData(), "}", first);
		}

		first = append(sb, "dat=", intent.getData(), "", first);
		first = append(sb, "typ=", intent.getType(), "", first);

		first = append(sb, "act=", intent.getAction(), "", first);
		first = append(sb, "cat=", intent.getCategories(), "", first);
		//noinspection ResourceType TODO external annotations to Intent#getFlags?
		first = append(sb, "flg=", IntentFlags.Converter.toString(intent.getFlags(), null), "", first);

		first = append(sb, "bnds=", intent.getSourceBounds(), "", first);
		return sb.toString();
	}
	private static boolean append(StringBuilder b, String prefix, Object data, String suffix,
			boolean first) {
		if (data != null) {
			if (!first) {
				b.append(' ');
			}
			b.append(prefix).append(data).append(suffix);
			first = false;
		}
		return first;
	}

	@DebugHelper
	public static String toString(Object value) {
		if (value == null) {
			return NULL;
		}
		String type = value.getClass().getName();
		String display;
		if (value instanceof Bundle) {
			display = toString((Bundle)value, " ", "#{", "", ", ", "}");
		} else if (value instanceof Intent) {
			display = toString((Intent)value);
		} else if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT && value instanceof android.app.Fragment.SavedState) {
			return "(SavedState)" + toString(ReflectionTools.get(value, "mState"));
		} else if (value instanceof android.support.v4.app.Fragment.SavedState) {
			return "(v4.SavedState)" + toString(ReflectionTools.get(value, "mState"));
		} else {
			display = value.toString();
			if (type.length() <= display.length() && display.startsWith(type)) {
				display = display.substring(type.length()); // from @ sign or { in case of View
			}
			display = shortenPackageNames(display);
		}
		return "(" + shortenPackageNames(type) + ")" + display;
	}

	@DebugHelper
	private static String shortenPackageNames(String string) {
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+(v4|v7|v13)\\.(?:[a-z0-9]+\\.)+", "$1.");
		string = string.replaceAll("^android\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^javax?\\.(?:[a-z0-9]+\\.)+", "");
		string = string.replaceAll("^net\\.twisterrob\\.([a-z0-9.]+\\.)+", "tws.");
		return string;
	}

	@SuppressWarnings("deprecation")
	public static android.hardware.Camera.Size getOptimalSize(List<android.hardware.Camera.Size> sizes, int w, int h) {
		if (sizes == null) {
			return null;
		}

		android.hardware.Camera.Size optimalSize = findClosestAspect(sizes, w, h, 0.1);

		if (optimalSize == null) {
			optimalSize = findClosestAspect(sizes, w, h, Double.POSITIVE_INFINITY);
		}

		return optimalSize;
	}

	@SuppressWarnings("deprecation")
	private static android.hardware.Camera.Size findClosestAspect(
			List<android.hardware.Camera.Size> sizes, int width, int height, double tolerance) {
		android.hardware.Camera.Size optimalSize = null;

		final double targetRatio = (double)width / (double)height;
		double minDiff = Double.MAX_VALUE;
		for (android.hardware.Camera.Size size : sizes) {
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
	public static int calculateRotation(Context context, android.hardware.Camera.CameraInfo cameraInfo) {
		WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = windowManager.getDefaultDisplay().getRotation();
		int degrees = rotation * 90; // consider using Surface.ROTATION_ constants

		int result;
		if (cameraInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
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
	 * @see AsyncTask#execute(Object[])
	 * @see <a href="http://commonsware.com/blog/2012/04/20/asynctask-threading-regression-confirmed.html">AsyncTask Threading Regression Confirmed</a>
	 * @see <a href="https://groups.google.com/forum/#!topic/android-developers/8M0RTFfO7-M">AsyncTask in Android 4.0</a>
	 * @see <a href="http://www.jayway.com/2012/11/28/is-androids-asynctask-executing-tasks-serially-or-concurrently/">AsyncTask ordering</a>
	 */
	@SafeVarargs
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static <Params> void executeParallel(final AsyncTask<Params, ?, ?> as, final Params... params) {
		if (!isOnUIThread()) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override public void run() {
					executeParallel(as, params);
				}
			});
		}
		if (VERSION.SDK_INT < VERSION_CODES.DONUT) {
			throw new IllegalStateException("Cannot execute AsyncTask in parallel before DONUT");
		} else if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			as.execute(params); // default is pooling, cannot be explicit
		} else {
			as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params); // default is serial, explicit pooling
		}
	}

	/**
	 * @see AsyncTask#execute(Object[])
	 * @see #executeParallel(AsyncTask, Object[])
	 */
	@SafeVarargs
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public static <Params> void executeSerial(final AsyncTask<Params, ?, ?> as, final Params... params) {
		if (!isOnUIThread()) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override public void run() {
					executeSerial(as, params);
				}
			});
		}
		if (VERSION.SDK_INT < VERSION_CODES.DONUT) {
			as.execute(params); // default is serial
		} else if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			throw new IllegalStateException("Cannot execute AsyncTask in serial between DONUT and HONEYCOMB");
		} else {
			as.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, params); // default is serial, explicit serial
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
						+ "\nAlso make sure that in the merged manifest the class name resolves correctly (package).");
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

	public static void setEnabled(MenuItem item, boolean enabled) {
		item.setEnabled(enabled);
		Drawable icon = item.getIcon();
		if (icon != null) {
			icon = icon.mutate();
			icon.setAlpha(enabled? 0xFF : 0x80);
			item.setIcon(icon);
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
	@DebugHelper
	public static String toIntentFlagString(@IntentFlags int level) {
		return IntentFlags.Converter.toString(level, true);
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
	public static ParcelFileDescriptor stream(final byte[] contents) throws FileNotFoundException {
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
				executeParallel(new AsyncTask<Object, Object, Object>() {
					@Override
					protected Object doInBackground(Object... params) {
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
	public static void dumpBackStack(Context context, FragmentManager fm) {
		int count = fm.getBackStackEntryCount();
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.ROOT, "There are %d entries in the backstack of %s:", count, fm));
		for (int i = 0; i < count; ++i) {
			BackStackEntry entry = fm.getBackStackEntryAt(i);

			int id = entry.getId();
			String name = entry.getName();
			CharSequence title = entry.getBreadCrumbTitle();
			CharSequence shortTitle = entry.getBreadCrumbShortTitle();
			String titleRes = resourceIdToString(context, entry.getBreadCrumbTitleRes());
			String shortTitleRes = resourceIdToString(context, entry.getBreadCrumbShortTitleRes());

			sb.append(String.format(Locale.ROOT, "\n\t#%d @%d: %s. shortTitle=(%s)%s, title=(%s)%s",
					i, id, name, shortTitleRes, shortTitle, titleRes, title));
		}
		LOG.trace(sb.toString());
	}
	private static String resourceIdToString(Context context, @AnyRes int shortTitleRes) {
		if (shortTitleRes != INVALID_RESOURCE_ID) {
			try {
				return context.getResources().getResourceName(shortTitleRes)
						+ " (" + Integer.toHexString(shortTitleRes) + ")";
			} catch (NotFoundException ex) {
				return Integer.toHexString(shortTitleRes);
			}
		}
		return null;
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

	public interface PopupCallbacks<T> {
		void finished(T value);
	}

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
					@Override
					public void onViewAttachedToWindow(View view) {
						view.removeOnAttachStateChangeListener(this);
						view.post(tryAgain);
					}

					@Override
					public void onViewDetachedFromWindow(View view) {
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
		return new AlertDialog.Builder(context)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						callbacks.finished(true);
					}
				})
				.setNegativeButton(android.R.string.cancel, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						callbacks.finished(false);
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
					Class providerClass = Class.forName(p.name);
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
