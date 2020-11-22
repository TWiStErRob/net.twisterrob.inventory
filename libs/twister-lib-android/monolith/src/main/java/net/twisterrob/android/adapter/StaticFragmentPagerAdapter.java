package net.twisterrob.android.adapter;

import java.util.*;

import androidx.fragment.app.*;

public class StaticFragmentPagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> fragments = new ArrayList<>();
	private final List<String> fragmentTitles = new ArrayList<>();
	public StaticFragmentPagerAdapter(FragmentManager manager) {
		super(manager);
	}

	public <T extends Fragment> T add(String title, T fragment) {
		fragments.add(fragment);
		fragmentTitles.add(title);
		return fragment;
	}

	@Override public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override public int getCount() {
		return fragments.size();
	}

	@Override public CharSequence getPageTitle(int position) {
		return fragmentTitles.get(position);
	}
}
