package net.twisterrob.inventory.android.test.actors;

import static org.hamcrest.Matchers.*;

import static android.support.test.espresso.intent.Intents.*;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;

import net.twisterrob.inventory.android.activity.BackupActivity;

public class BackupActivityActor extends ActivityActor {
	public BackupActivityActor() {
		super(BackupActivity.class);
	}

	public void openedViaIntent() {
		intended(allOf(isInternal(), hasComponent(BackupActivity.class.getName())));
	}
}
