package net.twisterrob.android.test;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;

/**
 * Equivalent to:
 * <code><pre>
 * adb shell settings put global window_animation_scale 0
 * adb shell settings put global transition_animation_scale 0
 * adb shell settings put global animator_duration_scale 0
 * </pre></code>
 * but requires
 * <code>adb shell pm grant app.package.id {@value #ANIMATION_PERMISSION}</code>
 * @see <a href="https://product.reverb.com/2015/06/06/disabling-animations-in-espresso-for-android-testing/">Disabling Animations</a>
 * @see <a href="https://gist.github.com/danielgomezrico/9371a79a7222a156ddad">Gist</a>
 */
public class SystemAnimations {
	private static final Logger LOG = LoggerFactory.getLogger(SystemAnimations.class);
	private static final String ANIMATION_PERMISSION = "android.permission.SET_ANIMATION_SCALE";
	private static final float DISABLED = 0.0f;
	private static final float DEFAULT = 1.0f;

	private static final Class<?> windowManagerStubClazz;
	private static final Method asInterface;
	private static final Class<?> serviceManagerClazz;
	private static final Method getService;
	private static final Class<?> windowManagerClazz;
	private static final Method setAnimationScales;
	private static final Method getAnimationScales;

	static {
		try {
			windowManagerStubClazz = Class.forName("android.view.IWindowManager$Stub");
			asInterface = windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder.class);
			serviceManagerClazz = Class.forName("android.os.ServiceManager");
			getService = serviceManagerClazz.getDeclaredMethod("getService", String.class);
			windowManagerClazz = Class.forName("android.view.IWindowManager");
			setAnimationScales = windowManagerClazz.getDeclaredMethod("setAnimationScales", float[].class);
			getAnimationScales = windowManagerClazz.getDeclaredMethod("getAnimationScales");
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}
	}

	private final Object windowManager;
	private final boolean canSetAnimationScales;
	private float[] previousScales;

	public SystemAnimations(Context context) {
		try {
			IBinder windowManagerBinder = (IBinder)getService.invoke(null, "window");
			windowManager = asInterface.invoke(null, windowManagerBinder);
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}
		int permStatus = context.checkCallingOrSelfPermission(ANIMATION_PERMISSION);
		canSetAnimationScales = permStatus == PackageManager.PERMISSION_GRANTED;
		if (canSetAnimationScales) {
			LOG.warn("Application doesn't have " + ANIMATION_PERMISSION);
		}
	}

	public void backup() {
		if (canSetAnimationScales) {
			previousScales = getScales();
		}
	}

	public void restore() {
		if (canSetAnimationScales) {
			setScales(previousScales);
		}
	}

	public void disableAll() {
		if (canSetAnimationScales) {
			setSystemAnimationsScale(DISABLED);
		}
	}

	public void enableAll() {
		if (canSetAnimationScales) {
			setSystemAnimationsScale(DEFAULT);
		}
	}

	public void setSystemAnimationsScale(float animationScale) {
		try {
			float[] currentScales = getScales();
			Arrays.fill(currentScales, animationScale);
			setScales(currentScales);
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}
	}
	public void setScales(float... currentScales) {
		try {
			setAnimationScales.invoke(windowManager, new Object[] {currentScales});
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}
	}
	public float[] getScales() {
		try {
			return (float[])getAnimationScales.invoke(windowManager);
		} catch (Throwable ex) {
			throw new IllegalStateException(ex);
		}
	}
}
