package net.twisterrob.android.test.espresso.idle;

import org.slf4j.*;

import android.support.test.InstrumentationRegistry;

import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.*;

public class GlideIdlingResource extends AsyncIdlingResource {
	private static final Logger LOG = LoggerFactory.getLogger(GlideIdlingResource.class);

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
		Glide glide = Glide.get(InstrumentationRegistry.getTargetContext());
		Engine engine = GlideAccessor.getEngine(glide);
		if (currentEngine != engine) {
			if (watcher != null) {
				watcher.unsubscribe(callTransitionToIdle);
			}
			EngineIdleWatcher oldWatcher = watcher;
			watcher = new EngineIdleWatcher(engine);
			watcher.setLogEvents(isVerbose());
			if (currentEngine != null) {
				LOG.warn("Engine changed from {}({}) to {}({})", currentEngine, oldWatcher, engine, watcher);
			}
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
}
