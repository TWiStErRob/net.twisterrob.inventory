package net.twisterrob.inventory.android;

import net.twisterrob.android.content.pref.ResourcePreferences;

public interface BaseComponent {
	ResourcePreferences prefs();
	
	interface Provider {
		BaseComponent getBaseComponent();
	}
}
