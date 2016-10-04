package net.twisterrob.android.test.espresso.idle;

import java.lang.reflect.Field;

import org.slf4j.*;

import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.*;

public class GlideIdlingResource extends AsyncIdlingResource {
	private static final Logger LOG = LoggerFactory.getLogger(GlideIdlingResource.class);
	private static final Field mEngine = getEngineField();

	private final Runnable callTransitionToIdle = new Runnable() {
		@Override public void run() {
			transitionToIdle();
		}
	};
	private EngineIdleWatcher watcher;
	private Engine currentEngine;

	@Override public String getName() {
		return "Glide";
	}

	@Override protected boolean isIdle() {
		// Glide is a singleton, hence Engine should be too; just lazily initialize when needed.
		// In case Glide is replaced, this will still work.
		Engine engine = getEngine();
		if (currentEngine != engine) {
			if (watcher != null) {
				watcher.unsubscribe(callTransitionToIdle);
			}
			EngineIdleWatcher oldWatcher = watcher;
			watcher = new EngineIdleWatcher(engine);
			watcher.setLogEvents(isVerbose());
			LOG.warn("Engine changed from {}({}) to {}({})", currentEngine, oldWatcher, engine, watcher);
			currentEngine = engine;
		}
		return isIdleCore();
	}

	private boolean isIdleCore() {
		return watcher.isIdle();
	}

	@Override protected void waitForIdleAsync() {
		watcher.subscribe(callTransitionToIdle);
	}

	@Override protected void transitionToIdle() {
		watcher.unsubscribe(callTransitionToIdle);
		super.transitionToIdle();
	}

	private @Nullable Engine getEngine() {
		try {
			Glide glide = Glide.get(InstrumentationRegistry.getTargetContext());
			return (Engine)mEngine.get(glide);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Glide Engine cannot be found", ex);
		}
	}

	private static Field getEngineField() {
		try {
			Field field = Glide.class.getDeclaredField("engine");
			field.setAccessible(true);
			return field;
		} catch (Exception ex) {
			throw new IllegalStateException("Glide Engine cannot be found", ex);
		}
	}
}
