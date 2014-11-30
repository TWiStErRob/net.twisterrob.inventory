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

		if (savedInstanceState == null) {
			editor = onCreateFragment(null);
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.activityRoot, editor)
			                           .commit()
			;
		} else {
			editor = findEditor();
		}
	}

	protected abstract E onCreateFragment(Bundle savedInstanceState);

	public E getEditor() {
		return editor;
	}

	@SuppressWarnings("unchecked")
	private E findEditor() {
		return (E)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}
}
