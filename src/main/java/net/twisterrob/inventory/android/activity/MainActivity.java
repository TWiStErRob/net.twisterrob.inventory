package net.twisterrob.inventory.android.activity;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.view.*;

import net.twisterrob.android.activity.CaptureImage;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.fragment.MainFragment;

public class MainActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setIcon(getResources().getDrawable(R.drawable.ic_launcher));

		setContentView(R.layout.generic_activity_drawer);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.activityRoot, MainFragment.newInstance())
			                           .commit()
			;

			if (Constants.DISABLE) {
				onOptionsItemSelected(new MenuBuilder(this).add(0, R.id.debug, 0, "Debug"));
			}
		}
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.preferences:
				startActivity(PreferencesActivity.show());
				return true;
			case R.id.debug:
				startActivityForResult(CaptureImage.saveTo(this, new File(getCacheDir(), "dev.jpg")), 32767);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 32767 && resultCode == RESULT_OK) {
			startActivity(ImageActivity.show(data.getData()));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	public static Intent home() {
		Intent intent = new Intent(App.getAppContext(), MainActivity.class);
		return intent;
	}
}
