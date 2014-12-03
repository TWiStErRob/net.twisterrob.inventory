package net.twisterrob.inventory.android.activity.data;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class BaseDetailActivity<C extends BaseFragment<?>> extends BaseActivity {
	private static final Logger LOG = LoggerFactory.getLogger(BaseDetailActivity.class);

	protected boolean wantDrawer;
	private C fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarSubtitle(getTitle());
		setActionBarTitle("...");

		super.setContentView(wantDrawer? R.layout.generic_activity_drawer : R.layout.generic_activity_nodrawer);

		String extrasError = checkExtras();
		if (extrasError != null) {
			App.toast(extrasError);
			finish();
			return;
		}

		if (savedInstanceState == null) {
			C fragment = onCreateFragment(null);
			if (fragment != null) {
				updateFragment(fragment).commit();
			}
			// TODO may need getSupportFragmentManager().executePendingTransactions(); ? but it works :)
		} else {
			fragment = findFragment();
		}
	}

	protected abstract C onCreateFragment(Bundle savedInstanceState);

	protected FragmentTransaction updateFragment(C fragment) {
		this.fragment = fragment;

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (fragment != null) {
			ft.replace(R.id.activityRoot, fragment);
		}
		return ft;
	}

	protected String checkExtras() {
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		fragment.refresh();
	}

	public C getFragment() {
		return fragment;
	}

	@SuppressWarnings("unchecked")
	private C findFragment() {
		return (C)getSupportFragmentManager().findFragmentById(R.id.activityRoot);
	}
}
