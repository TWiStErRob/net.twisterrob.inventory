package net.twisterrob.android.view;

import android.view.View;

public interface ViewProvider {
	View getView();

	class StaticViewProvider implements ViewProvider {
		private final View view;

		public StaticViewProvider(View view) {
			this.view = view;
		}

		@Override public View getView() {
			return view;
		}
	}
}
