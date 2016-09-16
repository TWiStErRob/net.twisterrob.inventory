package net.twisterrob.android.test.espresso.idle;

import java.lang.reflect.Field;
import java.util.*;

import org.slf4j.*;

import android.support.annotation.*;

import static android.support.test.InstrumentationRegistry.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.*;

import static net.twisterrob.java.utils.ReflectionTools.*;

public class GlideIdlingResource extends AsyncIdlingResource {
	private static final Logger LOG = LoggerFactory.getLogger(GlideIdlingResource.class);
	private final Runnable callTransitionToIdle = new Runnable() {
		@Override public void run() {
			//LOG.trace("{}(callTransitionToIdle).run", this);
			if (isIdleCore()) {
				transitionToIdle();
			}
		}
	};
	private final EngineDigger digger = new EngineDigger();
	private EngineWatcher current;

	@Override public String getName() {
		return "Glide-jobs";
	}
	@Override protected boolean isIdle() {
		// Glide is a singleton, hence Engine should be too; just lazily initialize when needed.
		// In case Glide is replaced, this will work anyway, because a new rule is created for every test method.
		EngineWatcher current = digger.getCurrent();
		if (this.current != current) {
			//LOG.trace("{}.isIdle, replacing {}({}) with {}({})",
			//		this, this.current, this.current != null? this.current.engine : null, current, current.engine);
			if (this.current != null) {
				this.current.unsubscribe(callTransitionToIdle);
			}
			this.current = current;
		}
		return isIdleCore();
	}
	private boolean isIdleCore() {
		Map<Key, ?> jobs = current.getJobs();
		//LOG.trace("{}.isIdle: {}", this, jobs);
		return jobs.isEmpty();
	}
	@Override protected void waitForIdleAsync() {
		//LOG.trace("{}.waitForIdleAsync", this);
		current.subscribe(callTransitionToIdle);
	}

	// Glide is a singleton, hence Engine should be too; just lazily initialize when needed.
	// In case Glide is replaced in the test this will take care of always giving the latest version.
	private static class EngineDigger {
		private static final Field mEngine = trySetAccessible(tryFindDeclaredField(Glide.class, "engine"));

		private final Map<Engine, EngineWatcher> engines = new HashMap<>();

		public @NonNull EngineWatcher getCurrent() {
			Engine engine = getEngine();
			EngineWatcher watcher = engines.get(engine);
			if (watcher == null) {
				watcher = new EngineWatcher(engine);
				engines.put(engine, watcher);
			}
			return watcher;
		}

		private @Nullable Engine getEngine() {
			if (mEngine == null) {
				return null;
			}
			try {
				Glide glide = Glide.get(getTargetContext());
				return (Engine)mEngine.get(glide);
			} catch (IllegalAccessException ex) {
				return null;
			}
		}
	}
}
