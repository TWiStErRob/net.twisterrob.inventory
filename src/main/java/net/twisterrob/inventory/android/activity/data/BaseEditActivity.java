package net.twisterrob.inventory.android.activity.data;

import android.os.Bundle;
import android.support.v4.app.*;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BaseDriveActivity;

public abstract class BaseEditActivity<E extends Fragment> extends BaseDriveActivity {
	private E editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_edit);

		editor = onCreateFragment(savedInstanceState);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.editor, editor);
		ft.commit();
	}

	protected abstract E onCreateFragment(Bundle savedInstanceState);

	public E getEditor() {
		return editor;
	}
}
