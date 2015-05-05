package net.twisterrob.inventory.android.sunburst;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.SingleFragmentActivity;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.sunburst.SunburstFragment.SunBurstEvents;

public class SunburstActivity extends SingleFragmentActivity implements SunBurstEvents {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setIcon(R.raw.ic_sunburst);
		setActionBarSubtitle(getTitle());
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	@Override protected CharSequence checkExtras() {
		if (getIntent().getExtras() == null) {
			return "Empty extras";
		}
		return super.checkExtras();
	}

	@Override protected BaseFragment onCreateFragment() {
		SunburstFragment fragment = SunburstFragment.newInstance();
		fragment.setArguments(getIntent().getExtras());
		return fragment;
	}

	@Override public void rootChanged(String name) {
		setActionBarTitle(name);
	}

	public static Intent displayProperty(long propertyID) {
		Intent intent = new Intent(App.getAppContext(), SunburstActivity.class);
		intent.replaceExtras(SunburstFragment.newPropertyInstance(propertyID).getArguments());
		return intent;
	}
	public static Intent displayRoom(long roomID) {
		Intent intent = new Intent(App.getAppContext(), SunburstActivity.class);
		intent.replaceExtras(SunburstFragment.newRoomInstance(roomID).getArguments());
		return intent;
	}
	public static Intent displayItem(long itemID) {
		Intent intent = new Intent(App.getAppContext(), SunburstActivity.class);
		intent.replaceExtras(SunburstFragment.newItemInstance(itemID).getArguments());
		return intent;
	}
}