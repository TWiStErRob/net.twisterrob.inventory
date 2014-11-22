package net.twisterrob.inventory.android.activity;

import java.io.File;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.fragment.BackupFragment;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;
import net.twisterrob.inventory.android.view.*;

public class MainActivity extends BaseDrawerActivity implements BackupPickerListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		setIcon(getResources().getDrawable(R.drawable.ic_launcher));

		getSupportFragmentManager() //
				.beginTransaction() //
				.add(new BackupFragment(), BackupFragment.class.getName()) //
				.commit() //
		;

		GridView list = (GridView)findViewById(android.R.id.list);
		list.setAdapter(new IconedItemAdapter(this, R.layout.main_item, createActions()));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IconedItem item = (IconedItem)parent.getItemAtPosition(position);
				startActivity(item.getIntent());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);

		getMenuInflater().inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(this, menu, R.id.search);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.preferences:
				startActivity(PreferencesActivity.show());
				return true;
			default:
				// let super do its thing
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void filePicked(File file) {
		BackupFragment backup = getFragment(BackupFragment.class.getName());
		backup.filePicked(file);
	}
}
