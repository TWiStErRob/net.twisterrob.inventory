package net.twisterrob.inventory.android.activity;

import java.util.*;

import android.content.Context;
import android.os.Bundle;

import com.android.debug.hv.ViewServer;

import net.twisterrob.android.utils.log.LoggingActivity;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.view.IconedItem;

public class VariantActivity extends LoggingActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).addWindow(this);
		}
	}

	@Override protected void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).setFocusedWindow(this);
		}
	}

	@Override protected void onDestroy() {
		if (BuildConfig.DEBUG) {
			ViewServer.get(this).removeWindow(this);
		}
		super.onDestroy();
	}

	protected Collection<IconedItem> createActions() {
		Collection<IconedItem> acts = new ArrayList<>();

		acts.add(new SVGIntentItem(Constants.INVALID_RESOURCE_ID, R.raw.category_chip, this, DeveloperActivity.show()) {
			@Override public CharSequence getTitle(Context context) {
				return "Developer Tools";
			}
		});

		return acts;
	}
}
