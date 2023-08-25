package net.twisterrob.inventory.android.hacks;

import java.lang.reflect.Field;

public class ViewCompatHacks {
	/**
	 * Replace the {@link java.util.WeakHashMap} in {@link androidx.core.view.ViewCompat} with a
	 * {@link DeferredWeakHashMapFor293190504} to prevent concurrent modification.
	 *
	 * @see <a href="https://issuetracker.google.com/issues/293190504">Issue</a>
	 * @see <a href="https://github.com/TWiStErRob/net.twisterrob.inventory/issues/302">Issue</a>
	 */
	public static void patchFor293190504() {
		try {
			Field sAccessibilityPaneVisibilityManager = Class
					.forName("androidx.core.view.ViewCompat")
					.getDeclaredField("sAccessibilityPaneVisibilityManager");
			sAccessibilityPaneVisibilityManager.setAccessible(true);
			Field mPanesToVisible = Class
					.forName("androidx.core.view.ViewCompat$AccessibilityPaneVisibilityManager")
					.getDeclaredField("mPanesToVisible");
			mPanesToVisible.setAccessible(true);

			Object target = sAccessibilityPaneVisibilityManager.get(null);
			mPanesToVisible.set(target, new DeferredWeakHashMapFor293190504<>());
		} catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
}
