package net.twisterrob.android.test.espresso.idle;

import java.lang.reflect.Method;
import java.util.*;

import javax.inject.Provider;

import org.hamcrest.Matcher;

import android.annotation.TargetApi;
import android.os.Build.*;
import android.os.Looper;
import android.support.test.espresso.Root;
import android.support.test.espresso.base.RootsOracle_Factory;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;

import net.twisterrob.android.test.espresso.DialogMatchers;

import static net.twisterrob.java.utils.ReflectionTools.*;

public class ToastIdlingResource extends AsyncIdlingResource {
	private static final Class<?> RootsOracle = forName("android.support.test.espresso.base.RootsOracle");
	private static final Method listRoots = tryFindDeclaredMethod(RootsOracle, "listActiveRoots");

	@Override public String getName() {
		return "Toast";
	}

	@Override protected boolean isIdle() {
		return getToast() == null;
	}

	@TargetApi(VERSION_CODES.HONEYCOMB_MR1)
	@Override protected void waitForIdleAsync() {
		Root toast = getToast();
		if (toast != null && VERSION_CODES.HONEYCOMB_MR1 <= VERSION.SDK_INT) {
			toast.getDecorView().addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
				@Override public void onViewAttachedToWindow(View v) {
					v.removeOnAttachStateChangeListener(this);
					throw new IllegalStateException("Toast shouldn't be re-attached.");
				}
				@Override public void onViewDetachedFromWindow(View v) {
					v.removeOnAttachStateChangeListener(this);
					transitionToIdle();
				}
			});
		} else {
			// let Espresso do its usual exponential backoff thing
		}
	}

	private Root getToast() {
		Matcher<Root> toast = DialogMatchers.isToast();
		List<Root> roots = getRoots();
		for (Root root : roots) {
			if (toast.matches(root)) {
				return root;
			}
		}
		return null;
	}

	private List<Root> getRoots() {
		if (listRoots == null) {
			return Collections.emptyList();
		}
		Object oracle = RootsOracle_Factory.create(new Provider<Looper>() {
			@Override public Looper get() {
				return Looper.getMainLooper();
			}
		}).get();
		try {
			@SuppressWarnings("unchecked") List<Root> roots = (List<Root>)listRoots.invoke(oracle);
			return roots;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
