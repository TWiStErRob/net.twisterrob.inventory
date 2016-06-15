package net.twisterrob.android.utils.tostring;

import java.util.*;

import org.slf4j.*;

import android.app.FragmentSavedStateStringer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.*;
import android.support.v4.widget.DrawerLayoutStateStringer;
import android.support.v7.widget.*;
import android.view.AbsSavedState;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tostring.stringers.detailed.*;
import net.twisterrob.android.utils.tostring.stringers.name.ResourceNameStringer;

public class StringerRepo {
	private static final Logger LOG = LoggerFactory.getLogger(StringerRepo.class);
	public static final StringerRepo INSTANCE = new StringerRepo();

	private final Map<Class<?>, Stringer<?>> stringers = new HashMap<>();

	private StringerRepo() {
		register(Object.class, new DefaultStringer());
		register(AbsSavedState.class, new AbsSavedStateStringer());
		register(android.support.design.widget.NavigationView.SavedState.class, new NavigationViewSavedStateStringer());
		register(android.support.v7.widget.LinearLayoutManager.SavedState.class,
				new LinearLayoutManagerSavedStateStringer());
		register(android.support.v7.widget.StaggeredGridLayoutManager.SavedState.class,
				new StaggeredGridLayoutManagerSavedStateStringer());
		register("android.support.v4.widget.DrawerLayout$SavedState", new DrawerLayoutStateStringer());
		register(android.support.v4.app.Fragment.SavedState.class, new SupportFragmentSavedStateStringer());
		register(android.app.Fragment.SavedState.class, new FragmentSavedStateStringer());
		register("android.support.v4.app.FragmentManagerState", new SupportFragmentManagerStateStringer());
		register(android.support.v4.content.Loader.class, new SupportLoaderStringer());
		register(android.content.Loader.class, new LoaderStringer());
		register(android.content.Intent.class, new IntentStringer());
		register(android.os.Bundle.class, new BundleStringer());
		register(android.support.v4.app.Fragment.class, new SupportFragmentStringer());
		register(android.support.v7.widget.RecyclerView.SavedState.class, new RecyclerViewSavedStateStringer());

		Context context = AndroidTools.getContext();
		register(android.support.v7.widget.Toolbar.SavedState.class, new SupportToolbarSavedStateStringer(context));
		register("android.support.v4.app.FragmentState", new SupportFragmentStateStringer(context));
		register("android.support.v4.app.BackStackState", new SupportBackStackStateStringer(context));
		register(Integer.class, new ResourceNameStringer(context));
		register(android.support.v4.app.FragmentManager.class, new FragmentManagerStringer());
		register(android.support.v4.app.FragmentManager.BackStackEntry.class, new BackStackEntryStringer(context));
	}

	public <T> void register(@NonNull String className, @NonNull Stringer<T> stringer) {
		try {
			@SuppressWarnings("unchecked")
			Class<? super T> clazz = (Class<? super T>)Class.forName(className);
			register(clazz, stringer);
		} catch (ClassNotFoundException e) {
			LOG.error("Cannot find class {} to register {}", className, stringer, e);
		}
	}

	public <T> void register(@NonNull Class<? super T> clazz, @NonNull Stringer<T> stringer) {
		stringers.put(clazz, stringer);
	}

	public @NonNull <T> Stringer<? super T> findByValue(@NonNull T value) {
		@SuppressWarnings("unchecked") Class<T> clazz = (Class<T>)value.getClass();
		return find(clazz);
	}

	@SuppressWarnings("unchecked")
	public @NonNull <T> Stringer<? super T> find(@NonNull Class<T> clazz) {
		Class<?> superClass = clazz;
		Stringer<?> toString = null;
		while (superClass != null) {
			toString = stringers.get(superClass);
			if (toString != null) {
				break;
			}
			superClass = superClass.getSuperclass();
		}
		if (toString == null || Object.class.equals(superClass)) {
			for (Class<?> iface : clazz.getInterfaces()) {
				Stringer<?> ifaceToString = stringers.get(iface);
				if (ifaceToString != null) {
					toString = ifaceToString;
					break;
				}
			}
		}
		if (toString == null) {
			throw new IllegalStateException("Cannot find Stringer for class " + clazz);
		}
		return (Stringer<? super T>)toString;
	}
}
