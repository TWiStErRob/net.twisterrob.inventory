package android.support.v4.widget;

import java.lang.reflect.Field;

import org.slf4j.*;

import android.content.Context;
import android.graphics.Canvas;

public class LoggingEdgeEffect implements EdgeEffectCompat.EdgeEffectImpl {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingEdgeEffect.class);

	private static final Object STATIC_ACCESS = null;
	protected final EdgeEffectCompat.EdgeEffectImpl effect;

	protected LoggingEdgeEffect(Object effect) {
		this.effect = (EdgeEffectCompat.EdgeEffectImpl)effect;
	}

	public static void install() {
		install(LoggingEdgeEffect.class);
	}

	protected static void install(Class<? extends EdgeEffectCompat.EdgeEffectImpl> clazz) {
		try {
			Field field = EdgeEffectCompat.class.getDeclaredField("IMPL");
			field.setAccessible(true);
			EdgeEffectCompat.EdgeEffectImpl IMPL = (EdgeEffectCompat.EdgeEffectImpl)field.get(STATIC_ACCESS);
			if (!clazz.isInstance(IMPL)) {
				IMPL = clazz.getDeclaredConstructor(Object.class).newInstance(IMPL);
				field.set(STATIC_ACCESS, IMPL);
			}
		} catch (Exception ex) {
			LOG.error("Cannot install {}", LoggingEdgeEffect.class, ex);
		}
	}

	@Override public Object newEdgeEffect(Context context) {
		Object result = effect.newEdgeEffect(context);
		LOG.trace("newEdgeEffect({}): {}", context, result);
		return result;
	}
	@Override public void setSize(Object edgeEffect, int width, int height) {
		LOG.trace("setSize({}, {}, {})", edgeEffect, width, height);
		effect.setSize(edgeEffect, width, height);
	}
	@Override public boolean isFinished(Object edgeEffect) {
		boolean result = effect.isFinished(edgeEffect);
		LOG.trace("isFinished({}): {}", edgeEffect, result);
		return result;
	}
	@Override public void finish(Object edgeEffect) {
		LOG.trace("finish({})", edgeEffect);
		effect.finish(edgeEffect);
	}
	@Override public boolean onPull(Object edgeEffect, float deltaDistance) {
		boolean result = effect.onPull(edgeEffect, deltaDistance);
		LOG.trace("onPull({}, {}): {}", edgeEffect, deltaDistance, result);
		return result;
	}
	@Override public boolean onPull(Object edgeEffect, float deltaDistance, float displacement) {
		boolean result = effect.onPull(edgeEffect, deltaDistance, displacement);
		LOG.trace("onPull({}, {}, {}): {}", edgeEffect, deltaDistance, result);
		return result;
	}
	@Override public boolean onRelease(Object edgeEffect) {
		boolean result = effect.onRelease(edgeEffect);
		LOG.trace("onRelease({}): {}", edgeEffect, result);
		return result;
	}
	@Override public boolean onAbsorb(Object edgeEffect, int velocity) {
		boolean result = effect.onAbsorb(edgeEffect, velocity);
		LOG.trace("onAbsorb({}, {}): {}", edgeEffect, velocity, result);
		return result;
	}
	@Override public boolean draw(Object edgeEffect, Canvas canvas) {
		boolean result = effect.draw(edgeEffect, canvas);
		LOG.trace("draw({}, {}): {}", edgeEffect, canvas, result);
		return result;
	}
}
