package net.twisterrob.inventory.android.activity.data;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import static android.view.ViewGroup.LayoutParams.*;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.fragment.BaseFragment;

public abstract class BaseDetailActivity<D extends BaseFragment<?>, L extends BaseFragment<?>>
		extends BaseActivity {
	private D details;
	private L children;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarSubtitle(getTitle());
		setActionBarTitle("...");

		super.setContentView(R.layout.activity_view);

		String extrasError = checkExtras();
		if (extrasError != null) {
			App.toast(extrasError);
			finish();
			return;
		}

		onCreateFragments(savedInstanceState);
		// TODO may need getSupportFragmentManager().executePendingTransactions(); ? but it works :)
	}

	/** Call {@link #setFragments} */
	protected abstract void onCreateFragments(Bundle savedInstanceState);

	@Override protected void onResumeFragments() {
		super.onResumeFragments();
		ListView list = (ListView)children.getView().findViewById(android.R.id.list);
		View details = findViewById(R.id.details);
		ViewGroup parent = (ViewGroup)details.getParent();
		if (parent != list && parent != null) {
			parent.removeView(details);
			details.setLayoutParams(new LayoutParams(MATCH_PARENT, AndroidTools.dipInt(this, 200)));
			list.addHeaderView(details);
		}
	}
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
		@SuppressLint("ValidFragment")
		private NoFragment() {
			// prevent instantiation, just for type
		}
	}
}
