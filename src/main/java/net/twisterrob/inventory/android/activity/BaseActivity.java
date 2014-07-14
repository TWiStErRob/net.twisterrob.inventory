package net.twisterrob.inventory.android.activity;

import android.os.Bundle;
import android.support.v7.app.*;

public class BaseActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		initActionBar();
	}

	private void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
