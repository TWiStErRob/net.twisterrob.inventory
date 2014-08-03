package net.twisterrob.inventory.android.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Toast;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class BaseDetailActivity<D extends BaseFragment<?>, L extends BaseFragment<?>> extends BaseActivity {
	private D details;
	private L children;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_view);

		String extrasError = checkExtras();
		if (extrasError != null) {
			Toast.makeText(this, extrasError, Toast.LENGTH_LONG).show();
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
			ft.add(R.id.details, detailsFragment);
		}
		if (childrenFragment != null) {
			ft.add(R.id.children, childrenFragment);
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

	protected static final class NoFragment extends BaseFragment<Void> {
		private NoFragment() {
			// prevent instantiation, just for type
		}
	}
}
