package net.twisterrob.android.content.glide;

import org.slf4j.*;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import androidx.annotation.NonNull;

import net.twisterrob.android.annotation.TrimMemoryLevel;
import net.twisterrob.android.utils.tools.StringerTools;

public class LoggingBitmapPool implements BitmapPool {
	private final @NonNull Logger LOG;
	private final @NonNull BitmapPool wrapped;

	public LoggingBitmapPool(@NonNull BitmapPool wrapped) {
		this(wrapped, LoggerFactory.getLogger("glide.BitmapPool"));
	}

	public LoggingBitmapPool(@NonNull BitmapPool wrapped,  @NonNull Logger logger) {
		this.wrapped = wrapped;
		LOG = logger;
	}

	@Override public int getMaxSize() {
		int result = wrapped.getMaxSize();
		LOG.trace("getMaxSize(): {}", result);
		return result;
	}
	@Override public void setSizeMultiplier(float sizeMultiplier) {
		LOG.trace("setSizeMultiplier({})", sizeMultiplier);
		wrapped.setSizeMultiplier(sizeMultiplier);
	}
	@Override public boolean put(Bitmap bitmap) {
		boolean result = wrapped.put(bitmap);
		LOG.trace("put({}): {}", StringerTools.toString(bitmap), result);
		return result;
	}
	@Override public Bitmap get(int width, int height, Config config) {
		Bitmap result = wrapped.get(width, height, config);
		LOG.trace("get({}, {}, {}): {}", width, height, config, StringerTools.toString(result));
		return result;
	}
	@Override public Bitmap getDirty(int width, int height, Config config) {
		Bitmap result = wrapped.getDirty(width, height, config);
		LOG.trace("getDirty({}, {}, {}): {}", width, height, config, StringerTools.toString(result));
		return result;
	}
	@Override public void clearMemory() {
		LOG.trace("clearMemory()");
		wrapped.clearMemory();
	}
	@Override public void trimMemory(@TrimMemoryLevel int level) {
		LOG.trace("trimMemory({})", StringerTools.toTrimMemoryString(level));
		wrapped.trimMemory(level);
	}
}
