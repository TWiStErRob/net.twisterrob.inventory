package net.twisterrob.android.view;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.view.View;

public interface ViewProvider {
	View getView();

	public class StaticViewProvider implements ViewProvider {
		private final View view;

		public StaticViewProvider(View view) {
			this.view = view;
		}

		@Override public View getView() {
			return view;
		}
	}

	public class SupportFragmentViewProvider implements ViewProvider {
		private final android.support.v4.app.Fragment fragment;

		public SupportFragmentViewProvider(android.support.v4.app.Fragment fragment) {
			this.fragment = fragment;
		}

		@Override public View getView() {
			return fragment.getView();
		}
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	public class FragmentViewProvider implements ViewProvider {
		private final android.app.Fragment fragment;

		public FragmentViewProvider(android.app.Fragment fragment) {
			this.fragment = fragment;
		}

		@Override public View getView() {
			return fragment.getView();
		}
	}
}
