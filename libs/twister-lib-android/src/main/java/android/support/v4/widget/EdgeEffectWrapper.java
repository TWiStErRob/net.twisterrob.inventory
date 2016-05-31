package android.support.v4.widget;

import java.lang.reflect.Field;

import org.slf4j.*;

import android.content.Context;
import android.graphics.Canvas;

import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class EdgeEffectWrapper implements EdgeEffectCompat.EdgeEffectImpl {
	private static final Logger LOG = LoggerFactory.getLogger(EdgeEffectWrapper.class);
	private final EdgeEffectCompat.EdgeEffectImpl effectImpl;

	/**
	 * @param effectImpl {@link EdgeEffectCompat.EdgeEffectImpl} object
	 *                   (type erased to {@link Object} because it's not public)
	 */
	public EdgeEffectWrapper(Object effectImpl) {
		this.effectImpl = (EdgeEffectCompat.EdgeEffectImpl)effectImpl;
	}
	public EdgeEffectCompat.EdgeEffectImpl getWrapped() {
		return effectImpl;
	}

	public Object newEdgeEffect(Context context) {
		return effectImpl.newEdgeEffect(context);
	}
	public void setSize(Object edgeEffect, int width, int height) {
		effectImpl.setSize(edgeEffect, width, height);
	}
	public boolean isFinished(Object edgeEffect) {
		return effectImpl.isFinished(edgeEffect);
	}
	public void finish(Object edgeEffect) {
		effectImpl.finish(edgeEffect);
	}
	public boolean onPull(Object edgeEffect, float deltaDistance) {
		return effectImpl.onPull(edgeEffect, deltaDistance);
	}
	public boolean onRelease(Object edgeEffect) {
		return effectImpl.onRelease(edgeEffect);
	}
	public boolean onAbsorb(Object edgeEffect, int velocity) {
		return effectImpl.onAbsorb(edgeEffect, velocity);
	}
	public boolean draw(Object edgeEffect, Canvas canvas) {
		return effectImpl.draw(edgeEffect, canvas);
	}
	public boolean onPull(Object edgeEffect, float deltaDistance, float displacement) {
		return effectImpl.onPull(edgeEffect, deltaDistance, displacement);
	}

	private static final Object STATIC_ACCESS = null;

	/**
	 * Class must have a constructor with {@link Object} as its sole parameter.
	 * @param clazz implementation for edge effect compatibility layer
	 */
	public static void install(Class<? extends EdgeEffectCompat.EdgeEffectImpl> clazz) {
		try {
			Field field = EdgeEffectCompat.class.getDeclaredField("IMPL");
			field.setAccessible(true);
			EdgeEffectCompat.EdgeEffectImpl IMPL = (EdgeEffectCompat.EdgeEffectImpl)field.get(STATIC_ACCESS);
			if (!clazz.isInstance(IMPL)) {
				IMPL = clazz.getDeclaredConstructor(Object.class).newInstance(IMPL);
				field.set(STATIC_ACCESS, IMPL);
			}
		} catch (Exception ex) {
			LOG.error("Cannot install {}", clazz, ex);
		}
	}
}
