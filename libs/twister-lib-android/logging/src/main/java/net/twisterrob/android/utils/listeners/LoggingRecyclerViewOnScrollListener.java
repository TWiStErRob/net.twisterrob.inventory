package net.twisterrob.android.utils.listeners;

import org.slf4j.*;

import androidx.recyclerview.widget.RecyclerView;

import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class LoggingRecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
	private static final Logger LOGGER = LoggerFactory.getLogger("RecyclerViewOnScroll");
	private final Logger LOG;

	public LoggingRecyclerViewOnScrollListener() {
		this(LOGGER);
	}

	public LoggingRecyclerViewOnScrollListener(Logger log) {
		LOG = log;
	}

	@Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		LOG.trace("onScrollStateChanged({}, {})", recyclerView, scrollStateToString(newState));
	}

	@Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		LOG.trace("onScrolled({}, {}, {})", recyclerView, dx, dy);
	}

	String scrollStateToString(int scrollState) {
		switch (scrollState) {
			case RecyclerView.SCROLL_STATE_IDLE:
				return "SCROLL_STATE_IDLE";
			case RecyclerView.SCROLL_STATE_DRAGGING:
				return "SCROLL_STATE_DRAGGING";
			case RecyclerView.SCROLL_STATE_SETTLING:
				return "SCROLL_STATE_SETTLING";
			default:
				return "Unknown SCROLL_STATE_*: " + scrollState;
		}
	}
}
