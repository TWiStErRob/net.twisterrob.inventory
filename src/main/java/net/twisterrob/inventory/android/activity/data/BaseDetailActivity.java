package net.twisterrob.inventory.android.activity.data;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.BaseDriveActivity;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class BaseDetailActivity<D extends BaseFragment<?>, L extends BaseFragment<?>>
		extends
			BaseDriveActivity {
	private D details;
	private L children;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_view);

		String extrasError = checkExtras();
		if (extrasError != null) {
			App.toast(extrasError);
			finish();
			return;
		}

		onCreateFragments(savedInstanceState);
	}

	/** Call {@link #setFragments} */
	protected abstract void onCreateFragments(Bundle savedInstanceState);

	protected String checkExtras() {
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (details != null) {
			details.refresh();
		}
		if (children != null) {
			children.refresh();
		}
	}

	protected void setFragments(D detailsFragment, L childrenFragment) {
		this.details = detailsFragment;
		this.children = childrenFragment;

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (detailsFragment != null) {
			ft.replace(R.id.details, detailsFragment);
		}
		if (childrenFragment != null) {
			ft.replace(R.id.children, childrenFragment);
		}
		ft.commit();
	}

	protected FragmentTransaction updateDetailsFragment(D detailsFragment) {
		this.details = detailsFragment;

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (detailsFragment != null) {
			ft.replace(R.id.details, detailsFragment);
		}
		return ft;
	}

	protected FragmentTransaction updateChildrenFragment(L childrenFragment) {
		this.children = childrenFragment;

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (childrenFragment != null) {
			ft.replace(R.id.children, childrenFragment);
		}
		return ft;
	}

	public D getDetails() {
		return details;
	}
	public L getChildren() {
		return children;
	}

	public void hideDetails() {
		findViewById(R.id.details).setVisibility(View.GONE);
	}
	public void hideChildren() {
		findViewById(R.id.children).setVisibility(View.GONE);
	}

	public static final class NoFragment extends BaseFragment<Void> {
		private NoFragment() {
			// prevent instantiation, just for type
		}
	}
}
