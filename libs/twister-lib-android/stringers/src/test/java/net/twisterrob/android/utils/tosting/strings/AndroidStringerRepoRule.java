package net.twisterrob.android.utils.tosting.strings;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import net.twisterrob.android.utils.tostring.stringers.AndroidStringerRepo;
import net.twisterrob.java.utils.tostring.StringerRepo;

public class AndroidStringerRepoRule implements TestRule {
	@Override public @NonNull Statement apply(final @NonNull Statement base, @NonNull Description description) {
		return new Statement() {
			@Override public void evaluate() throws Throwable {
				try {
					Context context = ApplicationProvider.getApplicationContext();
					StringerRepo.INSTANCE.clear();
					StringerRepo.INSTANCE.initDefaults();
					AndroidStringerRepo.init(StringerRepo.INSTANCE, context);
					base.evaluate();
				} finally {
					StringerRepo.INSTANCE.clear();
				}
			}
		};
	}
}
