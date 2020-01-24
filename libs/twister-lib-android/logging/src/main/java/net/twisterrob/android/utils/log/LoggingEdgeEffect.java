package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.widget.EdgeEffectWrapper;

import net.twisterrob.java.annotations.DebugHelper;

/**
 * Register in {@link android.app.Application#onCreate} via
 * <code>
 * EdgeEffectWrapper.install(LoggingEdgeEffect.class);
 * </code>
 *
 * @see EdgeEffectWrapper#install(Class)
 */
@DebugHelper
public class LoggingEdgeEffect extends EdgeEffectWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingEdgeEffect.class);

	public LoggingEdgeEffect(Object effect) {
		super(effect);
	}

	@Override public Object newEdgeEffect(Context context) {
		Object result = super.newEdgeEffect(context);
		LOG.trace("newEdgeEffect({}): {}", context, result);
		return result;
	}
	@Override public void setSize(Object edgeEffect, int width, int height) {
		LOG.trace("setSize({}, {}, {})", edgeEffect, width, height);
		super.setSize(edgeEffect, width, height);
	}
	@Override public boolean isFinished(Object edgeEffect) {
		boolean result = super.isFinished(edgeEffect);
		LOG.trace("isFinished({}): {}", edgeEffect, result);
		return result;
	}
	@Override public void finish(Object edgeEffect) {
		LOG.trace("finish({})", edgeEffect);
		super.finish(edgeEffect);
	}
	@Override public boolean onPull(Object edgeEffect, float deltaDistance) {
		boolean result = super.onPull(edgeEffect, deltaDistance);
		LOG.trace("onPull({}, {}): {}", edgeEffect, deltaDistance, result);
		return result;
	}
	@Override public boolean onPull(Object edgeEffect, float deltaDistance, float displacement) {
		boolean result = super.onPull(edgeEffect, deltaDistance, displacement);
		LOG.trace("onPull({}, {}, {}): {}", edgeEffect, deltaDistance, displacement, result);
		return result;
	}
	@Override public boolean onRelease(Object edgeEffect) {
		boolean result = super.onRelease(edgeEffect);
		LOG.trace("onRelease({}): {}", edgeEffect, result);
		return result;
	}
	@Override public boolean onAbsorb(Object edgeEffect, int velocity) {
		boolean result = super.onAbsorb(edgeEffect, velocity);
		LOG.trace("onAbsorb({}, {}): {}", edgeEffect, velocity, result);
		return result;
	}
	@Override public boolean draw(Object edgeEffect, Canvas canvas) {
		boolean result = super.draw(edgeEffect, canvas);
		LOG.trace("draw({}, {}): {}", edgeEffect, canvas, result);
		return result;
	}
}
