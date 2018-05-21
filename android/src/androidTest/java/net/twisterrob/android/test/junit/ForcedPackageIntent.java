package net.twisterrob.android.test.junit;

import android.app.Activity;
import android.content.*;
import android.support.annotation.*;

/**
 * Intent to force a context (i.e. packageName).
 * This is useful for hacking around {@link android.support.test.rule.ActivityTestRule},
 * which forces the context to be {@link android.support.test.InstrumentationRegistry#getTargetContext()}.
 */
public class ForcedPackageIntent extends Intent {

	private final @NonNull Context forcedContext;

	public ForcedPackageIntent(@NonNull Context context) {
		super(context, Activity.class);
		forcedContext = context;
	}

	@Override public @NonNull Intent setAction(String action) {
		if (!Intent.ACTION_MAIN.equals(action)) {
			return super.setAction(action);
		}
		return this;
	}

	@Override public @Nullable ComponentName getComponent() {
		ComponentName comp = super.getComponent();
		return comp != null && !Activity.class.getName().equals(comp.getClassName())? comp : null;
	}

	@Override public @NonNull Intent setComponent(@Nullable ComponentName component) {
		if (component == null) {
			return super.setComponent(null);
		} else {
			return super.setComponent(new ComponentName(forcedContext, component.getClassName()));
		}
	}

	@Override public @NonNull Intent setPackage(@Nullable String ignored) {
		return super.setPackage(ignored != null? forcedContext.getPackageName() : null);
	}

	@Override public @NonNull Intent setClassName(@NonNull String ignored, @NonNull String className) {
		return super.setClassName(forcedContext.getPackageName(), className);
	}

	@Override public @NonNull Intent setClassName(@NonNull Context ignored, @NonNull String className) {
		return super.setClassName(forcedContext, className);
	}

	@Override public @NonNull Intent setClass(@NonNull Context ignored, @NonNull Class<?> cls) {
		return super.setClass(forcedContext, cls);
	}
}
