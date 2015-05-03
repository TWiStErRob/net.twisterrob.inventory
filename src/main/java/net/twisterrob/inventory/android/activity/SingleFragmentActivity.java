package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class SingleFragmentActivity<F extends BaseFragment> extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(SingleFragmentActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		super.setContentView(R.layout.generic_activity_nodrawer);

		CharSequence extrasError = checkExtras();
		if (extrasError != null) {
			App.toast(extrasError);
			finish();
			return;
		}

		if (savedInstanceState == null) {
			F fragment = onCreateFragment();
			if (fragment != null) {
				updateFragment(fragment).commit();
			}
		}
	}

	protected FragmentTransaction updateFragment(F fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (fragment != null) {
			ft.replace(R.id.activityRoot, fragment);
		}
		return ft;
	}

	protected abstract F onCreateFragment();

	protected CharSequence checkExtras() {
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getFragment().refresh();
	}

	@SuppressWarnings("unchecked")
	public F getFragment() {
		return (F)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}
}
