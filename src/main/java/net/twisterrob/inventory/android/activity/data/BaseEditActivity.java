package net.twisterrob.inventory.android.activity.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BaseActivity;

public abstract class BaseEditActivity<E extends Fragment> extends BaseActivity {
	private E editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.generic_activity_nodrawer);

		editor = onCreateFragment(savedInstanceState);

		getSupportFragmentManager().beginTransaction()
		                           .replace(R.id.activityRoot, editor)
		                           .commit()
		;
	}

	protected abstract E onCreateFragment(Bundle savedInstanceState);

	public E getEditor() {
		return editor;
	}
}
