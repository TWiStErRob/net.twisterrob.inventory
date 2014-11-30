package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.PropertyViewActivity;
import net.twisterrob.inventory.android.fragment.MainFragment;

public class MainActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setIcon(getResources().getDrawable(R.drawable.ic_launcher));

		if (Constants.DISABLE) {
			startActivity(PropertyViewActivity.show(1L));
			finish();
			return;
		}

		setContentView(R.layout.generic_activity_drawer);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.activityRoot, MainFragment.newInstance())
			                           .commit()
			;
		}
	}

	public static Intent home() {
		Intent intent = new Intent(App.getAppContext(), MainActivity.class);
		return intent;
	}
}
