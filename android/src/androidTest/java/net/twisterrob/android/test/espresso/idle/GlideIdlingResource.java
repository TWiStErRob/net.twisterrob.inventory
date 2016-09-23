package net.twisterrob.android.test.espresso.idle;

import java.lang.reflect.Field;

import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.*;

public class GlideIdlingResource extends AsyncIdlingResource {
	private static final Field mEngine = tryGetEngineField();

	private final Runnable callTransitionToIdle = new Runnable() {
		@Override public void run() {
			transitionToIdle();
		}
	};
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final EngineIdleWatcher jobs = new EngineIdleWatcher();

	@Override public String getName() {
		return "Glide-jobs";
	}

	@Override protected boolean isIdle() {
		// Glide is a singleton, hence Engine should be too; just lazily initialize when needed.
		// In case Glide is replaced, this will still work.
		jobs.associateWith(getEngine());
		return isIdleCore();
	}

	private boolean isIdleCore() {
		return jobs.isIdle();
	}

	@Override protected void waitForIdleAsync() {
		jobs.subscribe(callTransitionToIdle);
	}

	@Override protected void transitionToIdle() {
		jobs.unsubscribe(callTransitionToIdle);
		super.transitionToIdle();
	}

	private @Nullable Engine getEngine() {
		if (mEngine == null) {
			return null;
		}
		try {
			Glide glide = Glide.get(InstrumentationRegistry.getTargetContext());
			return (Engine)mEngine.get(glide);
		} catch (IllegalAccessException ex) {
			return null;
		}
	}

	private static Field tryGetEngineField() {
		try {
			Field field = Glide.class.getDeclaredField("engine");
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			return null;
		}
	}
}
