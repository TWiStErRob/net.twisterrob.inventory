package net.twisterrob.inventory.android.content;

import net.twisterrob.inventory.android.App;

public class AppSingletonDatabaseActor extends DataBaseActor {
	public AppSingletonDatabaseActor() {
		super(App.db());
	}
}
