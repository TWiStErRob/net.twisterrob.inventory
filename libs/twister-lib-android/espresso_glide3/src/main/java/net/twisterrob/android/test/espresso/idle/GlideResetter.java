package net.twisterrob.android.test.espresso.idle;

import java.util.HashMap;

import org.slf4j.*;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.manager.RequestManagerRetriever;

import net.twisterrob.android.test.junit.InstrumentationExtensions;
import net.twisterrob.java.utils.ReflectionTools;

public class GlideResetter {
	private static final Logger LOG = LoggerFactory.getLogger(GlideResetter.class);

	public static void resetGlide(Context context) {
		LOG.info("Resetting Glide for {}", context);
		cleanupGlide(context);
		restrictGlide(context);
		forgetGlide();
	}

	private static void cleanupGlide(final Context context) {
		InstrumentationExtensions.runOnMainIfNecessary(new Runnable() {
			@Override public void run() {
				Glide.with(context).onDestroy();
				Glide.get(context).clearMemory();
			}
		});
		Glide.get(context).clearDiskCache();
	}

	private static void forgetGlide() {
		// make sure Glide.with(...) never returns the old one
		ReflectionTools.set(RequestManagerRetriever.get(), "applicationManager", null);
		// make sure Glide.get(...) never returns the old one
		ReflectionTools.setStatic(Glide.class, "glide", null);
	}

	private static void restrictGlide(Context context) {
		Glide glide = Glide.get(context);
		Engine engine = ReflectionTools.get(glide, "engine");
		assert engine != null;
		ReflectionTools.set(engine, "jobs", new HashMap<Object, Object>() {

			private static final long serialVersionUID = 0L;

			@Override public Object put(Object key, Object value) {
				throw new UnsupportedOperationException("This engine is dead.");
			}

			@Override public Object remove(Object key) {
				throw new UnsupportedOperationException("This engine is dead.");
			}
		});
	}
}
