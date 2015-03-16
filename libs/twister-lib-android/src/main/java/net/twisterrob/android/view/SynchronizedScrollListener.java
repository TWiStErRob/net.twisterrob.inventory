package net.twisterrob.android.view;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Listener for {@link RecyclerView} to synchronize the scroll with another view.
 * Mostly useful for headers where the {@link RecyclerView} has a placeholder and the header is on top of it.
 */
public class SynchronizedScrollListener extends OnScrollListener {
	public interface ViewProvider {
		View getView();
	}

	/**
	 * <li><code>0</code> means that the view will scroll just outside the screen
	 * and come back only when the list is towards it's beginning.
	 * <li><code>ratio = 1</code> means that the view will scroll just outside the screen
	 * and come back immediately on scroll up.
	 * <li><code>0 < ratio < 1</code> means that the view will scroll out slower and come back faster.
	 * <li><code>1 < ratio</code> means that the view will scroll out faster and come back slower.
	 * <li>negative values are not tested, probably useless</li>
	 */
	private final float ratio;
	private final ViewProvider view;

	/**
	 * @param ratio slowdown for leaving the screen
	 * @param list needed for ugly hack to handle rotation correctly
	 * @param view to adjust the top of, it's useful to use a provider when the view is not yet available or may change,
	 *             for example {@link android.support.v4.app.Fragment#getView()}
	 */
	@SuppressWarnings("deprecation")
	public SynchronizedScrollListener(float ratio, final RecyclerView list, ViewProvider view) {
		this.ratio = ratio;
		this.view = view;

		if (list != null) { // need to mock a scroll event when the first layout happens
			final ViewTreeObserver observer = list.getViewTreeObserver();
			observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override public void onGlobalLayout() {
					onScrolled(list, 0, 0);
					if (observer.isAlive()) {
						observer.removeGlobalOnLayoutListener(this);
					}
				}
			});
		}
	}

	@Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		View view = this.view.getView();
		if (view != null) {
			int top = view.getTop();
			int height = view.getHeight();
			int offset;
			if (ratio == 0) {
				View placeholder = recyclerView.getLayoutManager().findViewByPosition(0);
				if (placeholder == null) { // no first view
					offset = -(top + height); // offset the bottom to 0
				} else { // placeholder on screen, but may be half hidden
					offset = placeholder.getTop() - top; // level the two views
				}
			} else {
				float ratio = 0 <= dy? this.ratio : 1 / this.ratio;
				offset = (int)(-dy * ratio);
			}

			if (top + offset < -height) { // would scroll out of screen on top
				offset = -(top + height); // offset the bottom to 0
			}
			if (top + offset > 0) { // would scroll to far down
				offset = -top; // offset the top to 0
			}

			view.offsetTopAndBottom(offset);
		}
	}

	protected static class StaticViewProvider implements ViewProvider {
		private final View view;

		public StaticViewProvider(View view) {
			this.view = view;
		}

		@Override public View getView() {
			return view;
		}
	}
}
