package net.twisterrob.inventory.android.activity.data;

import android.os.Bundle;
import android.support.v4.app.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BaseActivity;

public abstract class BaseEditActivity<E extends Fragment> extends BaseActivity {
	private E editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.generic_activity_nodrawer);

		editor = onCreateFragment(savedInstanceState);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.activityRoot, editor);
		ft.commit();
	}

	protected abstract E onCreateFragment(Bundle savedInstanceState);

	public E getEditor() {
		return editor;
	}
}
