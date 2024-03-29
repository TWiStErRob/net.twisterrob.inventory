package net.twisterrob.inventory.android.sunburst;

import java.util.Stack;

import org.slf4j.*;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import androidx.annotation.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.activity.BackPressAware;
import net.twisterrob.android.graphics.SunburstDrawable;
import net.twisterrob.android.utils.concurrent.*;
import net.twisterrob.android.utils.tools.ViewTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.sunburst.Node.Type;
import net.twisterrob.inventory.android.sunburst.SunburstFragment.SunBurstEvents;
import net.twisterrob.inventory.android.view.RecyclerViewController;

@AndroidEntryPoint
public class SunburstFragment extends BaseFragment<SunBurstEvents> implements BackPressAware {
	private static final Logger LOG = LoggerFactory.getLogger(SunburstFragment.class);

	public interface SunBurstEvents {
		void rootChanged(String name);
	}

	private ImageView diagram;
	private SunburstDrawable<Node> sunburst;
	private NodeTreeWalker walker;

	private LoadTreeTask loadTreeTask;

	public SunburstFragment() {
		setDynamicResource(DYN_EventsClass, SunBurstEvents.class);
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int ignore = App.prefs().getInt(R.string.pref_sunburstIgnoreLevel, R.integer.pref_sunburstIgnoreLevel_default);
		walker = new NodeTreeWalker(ignore);
		sunburst = new SunburstDrawable<>(walker, new Paints());
	}

	@Override public @NonNull View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		View view = inflater.inflate(R.layout.fragment_sunburst, container, false);
		diagram = view.findViewById(R.id.diagram);
		return view;
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		diagram.setOnTouchListener(new Toucher());
		setLoading(true);
	}

	private void setLoading(final boolean isLoading) {
		final SwipeRefreshLayout progress = requireView().findViewById(R.id.progress_circular);
		RecyclerViewController.initializeProgress(progress);
		progress.post(new Runnable() {
			@Override public void run() {
				progress.setRefreshing(isLoading);
			}
		});
	}

	@Override public void onStart() {
		super.onStart();
		if (sunburst.getRoot() != null) {
			setLoading(false);
			refreshRoot();
			diagram.setImageDrawable(sunburst);
			return;
		}
		loadTreeTask = new LoadTreeTask();
		loadTreeTask.execute(createStartingNode());
	}

	@Override public void onResume() {
		super.onResume();
		refreshRoot();
	}
	@Override public void onDestroy() {
		super.onDestroy();
		if (loadTreeTask != null) {
			loadTreeTask.destroy();
			loadTreeTask = null;
		}
	}

	@Override public void onDestroyView() {
		setLoading(false);
		diagram.setImageDrawable(null); // unschedule and clear callback
		diagram = null;
		super.onDestroyView();
	}

	private final Stack<Node> stack = new Stack<>();

	public void setRoot(Node root) {
		Node prevRoot = sunburst.getRoot();
		if (root == null || prevRoot == root) {
			return;
		}
		if (prevRoot != null) {
			stack.add(prevRoot);
		}
		sunburst.setHighlighted(null);
		setRootInternal(root);
	}

	private boolean hasPreviousRoot() {
		return !stack.isEmpty();
	}

	private void setPreviousRoot() {
		Node root = stack.pop();
		sunburst.setHighlighted(sunburst.getRoot());
		setRootInternal(root);
	}
	private void setRootInternal(@NonNull Node root) {
		sunburst.setRoot(root);
		invalidateOptionsMenu();
		if (eventsListener != null) {
			eventsListener.rootChanged(root.getLabel());
		}
	}
	private void refreshRoot() {
		Node root = sunburst.getRoot();
		if (root != null) {
			setRootInternal(root);
		}
	}

	@Override public boolean onBackPressed() {
		if (hasPreviousRoot()) {
			setPreviousRoot();
			return true;
		}
		return false;
	}

	@Override public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.sunburst, menu);
	}

	@Override public void onPrepareOptionsMenu(@NonNull Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Node root = sunburst.getRoot();
		ViewTools.visibleIf(menu, R.id.action_sunburst_open, root != null && !isArgument(root));
		ViewTools.visibleIf(menu, R.id.action_sunburst_ignore, root != null && root.parent != null);
		ViewTools.visibleIf(menu, R.id.action_sunburst_ignore_reset, !walker.ignore.isEmpty());
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_sunburst_open) {
			Node root = sunburst.getRoot();
			Intent intent;
			switch (root.type) {
				case Property:
					intent = PropertyViewActivity.show(root.id);
					break;
				case Room:
					intent = RoomViewActivity.show(root.id);
					break;
				case Item:
					intent = ItemViewActivity.show(root.id);
					break;
				default:
					throw new IllegalStateException("Should have been disabled");
			}
			startActivity(intent);
			return true;
		} else if (id == R.id.action_sunburst_ignore) {
			Node node = sunburst.getRoot();
			walker.ignore.add(node);
			int count = node.getCount();
			while (node.parent != null) {
				node.parent.filteredCount += count;
				node = node.parent;
			}
			if (hasPreviousRoot()) {
				setPreviousRoot();
			}
			return true;
		} else if (id == R.id.action_sunburst_ignore_reset) {
			for (Node node : walker.ignore) {
				while (node.parent != null) {
					node.parent.filteredCount = 0;
					node = node.parent;
				}
			}
			walker.ignore.clear();
			refreshRoot();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private class Toucher implements OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					sunburst.setHighlighted(whichNode(event));
					return true;
				case MotionEvent.ACTION_UP:
					setRoot(whichNode(event));
					v.performClick();
					return true;
			}
			return false;
		}

		private Node whichNode(MotionEvent event) {
			float[] points = unProject(event);
			if (points != null) {
				return sunburst.at(points[0], points[1]);
			}
			return null;
		}
		private float[] unProject(MotionEvent event) {
			Matrix inverse = new Matrix();
			if (diagram.getImageMatrix().invert(inverse)) {
				float[] points = {event.getX(), event.getY()};
				inverse.mapPoints(points);
				return points;
			}
			return null;
		}
	}

	private boolean isArgument(Node root) {
		switch (root.type) {
			case Item:
				return getArgItemID() == root.id;
			case Room:
				return getArgRoomID() == root.id;
			case Property:
				return getArgPropertyID() == root.id;
			case Root:
				return getArgItemID() == Item.ID_ADD
						&& getArgRoomID() == Room.ID_ADD
						&& getArgPropertyID() == Property.ID_ADD;
		}
		return false;
	}

	private Node createStartingNode() {
		if (getArgItemID() != Item.ID_ADD) {
			return new Node(Type.Item, getArgItemID());
		}
		if (getArgRoomID() != Room.ID_ADD) {
			return new Node(Type.Room, getArgRoomID());
		}
		if (getArgPropertyID() != Property.ID_ADD) {
			return new Node(Type.Property, getArgPropertyID());
		}
		return new Node(Type.Root, -1);
	}

	private long getArgPropertyID() {
		return requireArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}
	private long getArgRoomID() {
		return requireArguments().getLong(Extras.ROOM_ID, Room.ID_ADD);
	}
	private long getArgItemID() {
		return requireArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	public static SunburstFragment newPropertyInstance(long propertyID) {
		SunburstFragment fragment = new SunburstFragment();
		fragment.setArguments(Intents.bundleFromProperty(propertyID));
		return fragment;
	}
	public static SunburstFragment newRoomInstance(long roomID) {
		SunburstFragment fragment = new SunburstFragment();
		fragment.setArguments(Intents.bundleFromRoom(roomID));
		return fragment;
	}
	public static SunburstFragment newItemInstance(long itemID) {
		SunburstFragment fragment = new SunburstFragment();
		fragment.setArguments(Intents.bundleFromItem(itemID));
		return fragment;
	}
	public static SunburstFragment newInstance() {
		SunburstFragment fragment = new SunburstFragment();
		fragment.setArguments(new Bundle());
		return fragment;
	}

	private class LoadTreeTask extends SimpleSafeAsyncTask<Node, Void, Node> {
		@Override protected void onPreExecute() {
			setLoading(true);
		}

		@Override protected Node doInBackground(Node param) {
			Node root = new TreeLoader().build(param);
			root.parent = null; // clear parent in case it was set (happens when deep linking)
			return root;
		}

		@Override protected void onResult(Node result, Node param) {
			if (isCancelledInternal()) {
				return;
			}
			setRoot(result);
			//noinspection ConstantConditions in this case super conditions apply and it can be null
			if (getView() != null) {
				setLoading(false);
				diagram.setImageDrawable(sunburst);
			}
		}

		@SuppressWarnings("deprecation")
		public void execute(@NonNull Node node) {
			// Overridden to hide deprecation warnings at call-site.
			super.execute(node);
		}

		@SuppressWarnings("deprecation")
		private boolean isCancelledInternal() {
			// Overridden to hide deprecation warnings at call-site.
			return super.isCancelled();
		}

		@Override protected void onError(@NonNull Exception ex, Node param) {
			LOG.error("Cannot build {}", param, ex);
			throw new RuntimeException("Cannot load " + param, ex);
		}

		@SuppressWarnings("deprecation")
		public void destroy() {
			// Overridden to hide deprecation warnings at call-site.
			super.cancel(true);
		}
	}
}
