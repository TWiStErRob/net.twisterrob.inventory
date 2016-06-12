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
import android.content.res.Resources;
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
import android.support.design.widget.*;
import android.support.design.widget.NavigationView.SavedState;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.widget.*;
import android.text.TextUtils;
import android.util.*;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import static android.util.TypedValue.*;

import net.twisterrob.android.annotation.*;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.collections.NullsSafeComparator;
import net.twisterrob.java.utils.*;

@SuppressWarnings("unused")
public /*static*/ abstract class AndroidTools {
	private static final Logger LOG = LoggerFactory.getLogger(AndroidTools.class);

	private static Context appContext;
	public static synchronized void setContext(Context context) {
		if (appContext != null || context == null) {
			throw new IllegalArgumentException("Cannot set context twice or to null");
		}
		appContext = context.getApplicationContext();
	}
	public static Context getContext() {
		return appContext;
	}

	private static final float CIRCLE_LIMIT = 359.9999f;
	private static final int INVALID_POSITION = -1;

	public static final @AnyRes int INVALID_RESOURCE_ID = 0;
	/** @see View#toString() */
	private static final int RESOURCE_ID_MASK = 0xff000000;
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
	public static Intent setInitialIntents(Intent intent, Collection<Intent> intents) {
		intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
		return intent;
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
	public static String toLongString(Bundle bundle) {
		return toString(bundle, "", " of ", "\n", "\t", "\n", "");
	}

	@DebugHelper
	public static String toShortString(Bundle bundle) {
		return toString(bundle, "(", ")", "#{", "", ", ", "}");
	}

	@DebugHelper
	private static String toString(Bundle bundle, String preType, String postType, String start, String preItem,
			String postItem,
			String end) {
		if (bundle == null) {
			return NULL;
		}
		StringBuilder sb = new StringBuilder();
		toStringRec(sb, 1, bundle, preType, postType, start, preItem, postItem, end);
		return sb.toString();
	}
	private static final Collection<String> RESOLVE_RESOURCE_ID_KEYS = new HashSet<>(Arrays.asList(
			"android:views", // savedInstanceState > android:viewHierarchyState
			"android:view_state",
			// (FragmentManagerState)android:support:fragments > FragmentManagerImpl.VIEW_STATE_TAG
			"android:menu:action_views", // savedInstanceState > NavigationView.SavedState
			"android:focusedViewId"
	));
	private static void toStringRec(StringBuilder sb, int level,
			Bundle bundle, String preType, String postType, String start, String preItem, String postItem, String end) {
		sb.append(preType).append(debugType(bundle)).append(postType);
		try {
			sb.append(bundle.size());
		} catch (RuntimeException ex) {
			LOG.error("Cannot unparcel Bundle for logging", ex);
			sb.append(ex.toString());
			return; // skip the rest, there's quite possible no data
		}
		if (bundle.size() == 0) {
			return;
		}
		boolean shortcut = bundle.size() <= 1;
		if (shortcut) {
			sb.append(": ");
		} else {
			sb.append(start);
		}
		int deeperLevel = shortcut? level : level + 1;
		TreeSet<String> sortedKeys = CollectionTools.newTreeSet(bundle.keySet(), new NullsSafeComparator<String>());
		for (Iterator<String> it = sortedKeys.iterator(); it.hasNext(); ) {
			String key = it.next();
			if (!shortcut) {
				for (int i = 0; i < level; i++) {
					sb.append(preItem);
				}
			}
			sb.append(key).append(" -> ");

			Object value = bundle.get(key);
			boolean resolveValueAsId = appContext != null && RESOLVE_RESOURCE_ID_KEYS.contains(key);
			if (value instanceof Bundle) {
				Bundle val = (Bundle)value;
				toStringRec(sb, deeperLevel, val, preType, postType, start, preItem, postItem, end);
			} else if (value instanceof SparseArray) {
				SparseArray<?> arr = (SparseArray<?>)value;
				toStringRec(sb, deeperLevel, arr, preType, postType, start, preItem, postItem, end, resolveValueAsId);
			} else {
				if (value instanceof Integer && (resolveValueAsId || ((Integer)value & RESOURCE_ID_MASK) != 0)) {
					sb.append(toNameString(appContext, (Integer)value));
				} else {
					sb.append(toString(value));
				}
			}
			if (it.hasNext()) {
				sb.append(postItem);
			}
		}
		if (!shortcut) {
			sb.append(end);
		}
	}
	private static void toStringRec(StringBuilder sb, int level, SparseArray<?> array, String preType, String postType,
			String start, String preItem, String postItem, String end, boolean resolveKeysAsIds) {
		sb.append(preType).append(debugType(array)).append(postType).append(array.size());
		boolean shortcut = array.size() <= 1;
		if (shortcut) {
			sb.append(": ");
		} else {
			sb.append(start);
		}
		int deeperLevel = shortcut? level : level + 1;
		for (int index = 0; index < array.size(); index++) {
			int arrayKey = array.keyAt(index);
			Object arrayValue = array.get(arrayKey);
			if (!shortcut) {
				for (int i = 0; i < level; i++) {
					sb.append(preItem);
				}
			}
			if (resolveKeysAsIds || (arrayKey & RESOURCE_ID_MASK) != 0) {
				sb.append(toNameString(appContext, arrayKey));
			} else {
				sb.append(arrayKey);
			}
			sb.append(" -> ");
			if (arrayValue instanceof Bundle) {
				toStringRec(sb, deeperLevel, (Bundle)arrayValue, preType, postType, start, preItem, postItem, end);
			} else if (arrayValue instanceof SparseArray) {
				SparseArray<?> arr = (SparseArray<?>)arrayValue;
				toStringRec(sb, deeperLevel, arr, preType, postType, start, preItem, postItem, end, false);
			} else {
				sb.append(toString(arrayValue));
			}
			if (index + 1 < array.size()) {
				sb.append(postItem);
			}
		}
		if (!shortcut) {
			sb.append(end);
		}
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@DebugHelper
	private static String toString(android.content.Loader<?> loader) {
		if (loader == null) {
			return NULL;
		}
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		return writer.toString();
	}
	@DebugHelper
	private static String toString(android.support.v4.content.Loader<?> loader) {
		if (loader == null) {
			return NULL;
		}
		StringWriter writer = new StringWriter();
		loader.dump("", null, new PrintWriter(writer), null);
		return writer.toString();
	}

	@TargetApi(VERSION_CODES.JELLY_BEAN)
	@DebugHelper
	@SuppressWarnings({"ConstantConditions", "UnusedAssignment"}) // just so all lines look similar
	public static String toShortString(Intent intent) {
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
		//noinspection ResourceType TOFIX external annotations to Intent#getFlags?
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
	@SuppressWarnings("ConstantConditions") // field are declared primitive so reflection can't return null
	public static String toString(android.support.v7.widget.Toolbar.SavedState state) {
		int expandedMenuItemId = ReflectionTools.get(state, "expandedMenuItemId");
		boolean isOverflowOpen = ReflectionTools.get(state, "isOverflowOpen");
		return String.format(Locale.ROOT,
				"Overflow open=%b, Expanded MenuItem=%s",
				isOverflowOpen, toNameString(getContext(), expandedMenuItemId));
	}

	@DebugHelper
	@SuppressWarnings("ConstantConditions") // field are declared primitive so reflection can't return null
	public static String toString(LinearLayoutManager.SavedState state) {
		int mAnchorPosition = ReflectionTools.get(state, "mAnchorPosition");
		int mAnchorOffset = ReflectionTools.get(state, "mAnchorOffset");
		boolean mAnchorLayoutFromEnd = ReflectionTools.get(state, "mAnchorLayoutFromEnd");
		return String.format(Locale.ROOT,
				"Anchor: {pos=%d, offset=%d, fromEnd=%b}",
				mAnchorPosition, mAnchorOffset, mAnchorLayoutFromEnd);
	}

	@DebugHelper
	@SuppressWarnings("ConstantConditions") // field are declared primitive so reflection can't return null
	public static String toString(StaggeredGridLayoutManager.SavedState state) {
		int mAnchorPosition = ReflectionTools.get(state, "mAnchorPosition");
		int mVisibleAnchorPosition = ReflectionTools.get(state, "mVisibleAnchorPosition");
		int mSpanOffsetsSize = ReflectionTools.get(state, "mSpanOffsetsSize");
		int[] mSpanOffsets = ReflectionTools.get(state, "mSpanOffsets");
		int mSpanLookupSize = ReflectionTools.get(state, "mSpanLookupSize");
		int[] mSpanLookup = ReflectionTools.get(state, "mSpanLookup");
//		List<LazySpanLookup.FullSpanItem> mFullSpanItems = ReflectionTools.get(state, "mFullSpanItems");
		boolean mReverseLayout = ReflectionTools.get(state, "mReverseLayout");
		boolean mLastLayoutRTL = ReflectionTools.get(state, "mLastLayoutRTL");
		return String.format(Locale.ROOT,
				"Anchor: {pos=%d, visPos=%d}, reverse=%b, RTL=%b, Spans: {offsets=%s, lookups=%s}",
				mAnchorPosition, mVisibleAnchorPosition, mReverseLayout, mLastLayoutRTL,
				toString(mSpanOffsetsSize, mSpanOffsets), toString(mSpanLookupSize, mSpanLookup));
	}

	private static String toString(int size, int... values) {
		if (size == 0 || values == null) {
			return "[]";
		} else if (size < 0) {
			return String.valueOf(size);
		}
		return Arrays.toString(values);
	}

	@DebugHelper
	public static String toString(Object value) {
		if (value == null) {
			return NULL;
		}
		String type = debugType(value);
		String display = null;
		if (value instanceof Bundle) {
			display = toLongString((Bundle)value);
		} else if (value instanceof Intent) {
			display = toShortString((Intent)value);
		} else if (value instanceof String) {
			display = '"' + (String)value + '"';
		} else if (value.getClass().isArray()) {
			display = ArrayTools.toString(value);
		} else if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && value instanceof android.app.Fragment.SavedState) {
			type = "Fragment.SavedState";
			display = toString(ReflectionTools.get(value, "mState"));
		} else if (value instanceof android.support.v4.app.Fragment.SavedState) {
			type = "v4.Fragment.SavedState";
			display = toString(ReflectionTools.get(value, "mState"));
		} else if (value instanceof android.support.v4.content.Loader<?>) {
			display = toString((android.support.v4.content.Loader<?>)value);
		} else if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB && value instanceof android.content.Loader<?>) {
			display = toString((android.content.Loader<?>)value);
		} else if (value == AbsSavedState.EMPTY_STATE) {
			type = null;
			display = "AbsSavedState.EMPTY_STATE";
		} else if (value instanceof RecyclerView.SavedState) {
			Parcelable mLayoutState = ReflectionTools.get(value, "mLayoutState");
			if (mLayoutState instanceof LinearLayoutManager.SavedState
					|| mLayoutState instanceof StaggeredGridLayoutManager.SavedState) {
				display = toString(mLayoutState);
			}
		} else if (value instanceof LinearLayoutManager.SavedState) {
			display = toString((LinearLayoutManager.SavedState)value);
		} else if (value instanceof StaggeredGridLayoutManager.SavedState) {
			display = toString((StaggeredGridLayoutManager.SavedState)value);
		} else if (value instanceof NavigationView.SavedState) {
			display = toString(((SavedState)value).menuState);
		} else if (SupportV4WidgetAccess.instanceOf(value)) {
			display = SupportV4WidgetAccess.toString(value);
		} else if (value instanceof android.support.v7.widget.Toolbar.SavedState) {
			display = toString((android.support.v7.widget.Toolbar.SavedState)value);
		} else if (SupportV4AppAccess.instanceOf(value)) {
			display = SupportV4AppAccess.toString(value);
		}
		if (display == null) {
			display = shortenPackageNames(value.toString());
			if (type != null && type.length() <= display.length() && display.startsWith(type)) {
				display = display.substring(type.length()); // from @ sign or { in case of View
			}
		}
		return (type != null? "(" + type + ")" : "") + display;
	}

	private static String debugType(Object value) {
		if (value == null) {
			return NULL;
		}
		String name = value.getClass().getCanonicalName();
		if (name == null) {
			name = value.getClass().toString();
		}
		return shortenPackageNames(name);
	}

	@DebugHelper
	public static String shortenPackageNames(String string) {
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
		int degrees = rotation * 90; // CONSIDER using Surface.ROTATION_ constants

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
	@DebugHelper
	public static String toNameString(Context context, @IdRes int id) {
		String name = toNameString(context.getResources(), id);
		if (name.startsWith(context.getPackageName())) {
			name = "app" + name.substring(context.getPackageName().length());
		}
		return name;
	}

	/**
	 * -1 will be resolved to {@code "NO_ID"}, 0 is {@code "invalid"}, everything else will be tried to be resolved.
	 * @see View#NO_ID
	 * @see Resources#getIdentifier
	 * @return Fully qualified name of the resource,
	 *         or special values: {@code "View.NO_ID"}, {@code "invalid"}, {@code "not-found::<id>"}
	 */
	@DebugHelper
	public static String toNameString(Resources resources, @IdRes int id) {
		if (id == View.NO_ID) {
			return "View.NO_ID";
		} else if (id == INVALID_RESOURCE_ID) {
			return "invalid";
		} else {
			try {
				return resources.getResourceName(id);
			} catch (Resources.NotFoundException ignore) {
				return "not-found::" + id;
			}
		}
	}

	@DebugHelper
	public static String toNameString(Fragment fragment) {
		return toNameString((Object)fragment)
				+ (fragment != null? "(" + ReflectionTools.get(fragment, "mWho") + ")" : "");
	}
	@DebugHelper
	public static String toNameString(Activity activity) {
		return toNameString((Object)activity);
	}
	@DebugHelper
	public static String toNameString(Object object) {
		if (object != null) {
			Class<?> clazz = object.getClass();
			String className = clazz.getSimpleName();
			if (TextUtils.isEmpty(className)) {
				className = clazz.getName();
				if (className != null) {
					className = className.substring(className.lastIndexOf('.'));
					if (className.endsWith(";")) {
						// unknown dimensioned array
						className = className.substring(0, className.length() - 1) + "[?]";
					}
				}
			}
			return className + "@" + StringTools.hashString(object);
		} else {
			return "<null>";
		}
	}
	@DebugHelper
	public static String toLongString(Fragment fragment) {
		if (fragment == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(toNameString(fragment)).append('[').append(fragment).append(']');
		sb.append(':').append(toString(fragment.getArguments())).append('\n');
		sb.append("view=").append(fragment.getView()).append('\n');
		sb.append("activity=").append(fragment.getActivity()).append('\n');
		sb.append("context=").append(fragment.getContext()).append('\n');
		sb.append("host=").append(fragment.getHost()).append('\n');
		appendState(sb, fragment.isDetached(), "detached", ", ");
		appendState(sb, fragment.isAdded(), "added", ", ");
		appendState(sb, fragment.isResumed(), "resumed", ", ");
		appendState(sb, fragment.isHidden(), "hidden", ", ");
		appendState(sb, fragment.isVisible(), "visible", "");
		appendState(sb, fragment.isMenuVisible(), "menu visible", ", ");
		appendState(sb, fragment.isInLayout(), "in layout", ", ");
		appendState(sb, fragment.isRemoving(), "removing", ", ");
		return sb.toString();
	}
	private static void appendState(StringBuilder sb, boolean condition, String conditionName, String separator) {
		if (!condition) {
			sb.append("not ");
		}
		sb.append(conditionName);
		sb.append(separator);
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
