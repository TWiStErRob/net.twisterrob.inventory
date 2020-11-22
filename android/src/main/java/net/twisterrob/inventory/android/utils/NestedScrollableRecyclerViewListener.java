package net.twisterrob.inventory.android.utils;

import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;

import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;

public class NestedScrollableRecyclerViewListener extends SimpleOnGestureListener implements OnItemTouchListener {
	private final RecyclerView rv;
	private final GestureDetector detector;
	public NestedScrollableRecyclerViewListener(RecyclerView rv) {
		this.rv = rv;
		this.detector = new GestureDetector(rv.getContext(), this);
	}

	@Override public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		rv.getParent().requestDisallowInterceptTouchEvent(true);
		detector.onTouchEvent(e);
		return false;
	}

	@Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return distanceY < 0 && handleTop() || distanceY > 0 && handleBottom();
	}

	@Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return velocityY < 0 && handleTop() || velocityY > 0 && handleBottom();
	}

	private boolean handleBottom() {
		LinearLayoutManager manager = (LinearLayoutManager)rv.getLayoutManager();
		int last = rv.getAdapter().getItemCount() - 1;
		boolean lastVisible = manager.findLastCompletelyVisibleItemPosition() == last;
		if (lastVisible) {
			rv.getParent().requestDisallowInterceptTouchEvent(false);
			return true;
		}
		return false;
	}

	private boolean handleTop() {
		LinearLayoutManager manager = (LinearLayoutManager)rv.getLayoutManager();
		boolean firstVisible = manager.findFirstCompletelyVisibleItemPosition() == 0;
		if (firstVisible) {
			rv.getParent().requestDisallowInterceptTouchEvent(false);
			return true;
		}
		return false;
	}

	@Deprecated @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) { /* */ }
	@Deprecated @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { /* */ }
}
