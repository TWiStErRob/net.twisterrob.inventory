package net.twisterrob.inventory.android.fragment;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.IconedItem.IntentLauncher;

public class MainFragment extends BaseFragment<Void> implements BackupPickerListener {
	private static final String BACKUP_FRAGMENT = BackupFragment.class.getSimpleName();

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getChildFragmentManager().beginTransaction()
		                         .add(new BackupFragment(), BACKUP_FRAGMENT)
		                         .commit()
		;

		GridView list = (GridView)view.findViewById(android.R.id.list);
		list.setAdapter(new IconedItemAdapter(getContext(), R.layout.main_item, BaseActivity.createActions()));
		list.setOnItemClickListener(new IntentLauncher(getActivity()));
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);

		inflater.inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(getActivity(), menu, R.id.search);
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
		BackupFragment backup = (BackupFragment)getChildFragmentManager().findFragmentByTag(BACKUP_FRAGMENT);
		backup.filePicked(file);
	}

	public static Intent home() {
		Intent intent = new Intent(App.getAppContext(), MainFragment.class);
		return intent;
	}
	public static MainFragment newInstance() {
		MainFragment fragment = new MainFragment();
		return fragment;
	}
}
