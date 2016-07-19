package net.twisterrob.android.utils.tools;

import java.lang.reflect.Constructor;
import java.util.*;

import org.easymock.internal.ReflectionUtils;

/** Some idiot made the Size class a non-static inner class of Camera... and there's also no toString on it. */
@SuppressWarnings("deprecation")
/*default*/ class CameraSizeHelper {
	private static final android.hardware.Camera CAMERA;

	static {
		try {
			Constructor<android.hardware.Camera> ctor = ReflectionUtils.getConstructor(android.hardware.Camera.class);
			ctor.setAccessible(true);
			CAMERA = ctor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static android.hardware.Camera.Size s(int w, int h) {
		android.hardware.Camera.Size size = CAMERA.new Size(w, h);
		size.width = w;
		size.height = h;
		return size;
	}

	public static void printSizes(List<android.hardware.Camera.Size> sizes) {
		for (int i = 0; i < sizes.size(); i++) {
			android.hardware.Camera.Size size = sizes.get(i);
			float longer = size.width;
			float shorter = size.height;
			boolean land = size.width > size.height;
			if (longer < shorter) {
				float temp = shorter;
				shorter = longer;
				longer = temp;
			}
			System.out.printf(Locale.ROOT, "%d: %dx%d(land%s=%.3f, port%s=%.3f)",
					i, size.width, size.height,
					land? "*" : "", longer / shorter, land? "" : "*", shorter / longer);
			System.out.println();
		}
	}
}
