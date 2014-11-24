package net.twisterrob.inventory.android.activity.data;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.fragment.data.SunburstFragment;

public class SunburstActivity extends BaseActivity implements SunburstFragment.Listener {
	private SunburstFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setIcon(R.raw.ic_sunburst);

		setContentView(R.layout.generic_activity_drawer);
		getSupportFragmentManager().beginTransaction()
		                           .add(R.id.activityRoot, fragment = SunburstFragment.newInstance())
		                           .commit()
		;
	}

	@Override public void rootChanged(SunburstFragment.Node root) {
		setActionBarSubtitle(root.getLabel());
	}

	@Override
	public void onBackPressed() {
		if (fragment.hasPreviousRoot()) {
			fragment.setPreviousRoot();
			return;
		}

		super.onBackPressed();
	}

	public static Intent showAll() {
		Intent intent = new Intent(App.getAppContext(), SunburstActivity.class);
		return intent;
	}
}
