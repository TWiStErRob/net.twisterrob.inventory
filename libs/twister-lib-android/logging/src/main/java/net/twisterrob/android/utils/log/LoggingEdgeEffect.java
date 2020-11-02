package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION_CODES;
import android.support.annotation.*;
import android.widget.EdgeEffect;

import net.twisterrob.android.utils.wrappers.EdgeEffectWrapper;
import net.twisterrob.java.annotations.DebugHelper;

/**
 * @see android.support.v7.widget.RecyclerView#setEdgeEffectFactory
 */
@RequiresApi(VERSION_CODES.ICE_CREAM_SANDWICH)
@DebugHelper
public class LoggingEdgeEffect extends EdgeEffectWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingEdgeEffect.class);

	public LoggingEdgeEffect(@NonNull Context context, @NonNull EdgeEffect effect) {
		super(context, effect);
	}

	@Override public void setSize(int width, int height) {
		LOG.trace("{}.setSize({}, {})", getWrapped(), width, height);
		super.setSize(width, height);
	}

	@Override public boolean isFinished() {
		boolean result = super.isFinished();
		LOG.trace("{}.isFinished(): {}", getWrapped(), result);
		return result;
	}

	@Override public void finish() {
		LOG.trace("{}.finish()", getWrapped());
		super.finish();
	}

	@Override public void onPull(float deltaDistance) {
		LOG.trace("{}.onPull({})", getWrapped(), deltaDistance);
		super.onPull(deltaDistance);
	}

	@Override public void onPull(float deltaDistance, float displacement) {
		LOG.trace("{}.onPull({}, {})", getWrapped(), deltaDistance, displacement);
		super.onPull(deltaDistance, displacement);
	}

	@Override public void onRelease() {
		LOG.trace("{}.onRelease()", getWrapped());
		super.onRelease();
	}

	@Override public void onAbsorb(int velocity) {
		LOG.trace("{}.onAbsorb({})", getWrapped(), velocity);
		super.onAbsorb(velocity);
	}

	@Override public int getMaxHeight() {
		int result = super.getMaxHeight();
		LOG.trace("{}.getMaxHeight(): {}", getWrapped(), result);
		return result;
	}

	@Override public void setColor(int color) {
		LOG.trace("{}.setColor({})", getWrapped(), color);
		super.setColor(color);
	}

	@Override public int getColor() {
		int result = super.getColor();
		LOG.trace("{}.getColor(): {}", getWrapped(), result);
		return result;
	}

	@Override public boolean draw(Canvas canvas) {
		boolean result = super.draw(canvas);
		LOG.trace("{}.draw({}): {}", getWrapped(), canvas, result);
		return result;
	}
}
