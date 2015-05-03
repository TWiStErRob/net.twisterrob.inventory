package net.twisterrob.inventory.android.sunburst;

import java.util.Stack;

import org.slf4j.*;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.*;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import net.twisterrob.android.activity.BackPressAware;
import net.twisterrob.android.graphics.SunburstDrawable;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.sunburst.Node.Type;
import net.twisterrob.inventory.android.sunburst.SunburstFragment.SunBurstEvents;

public class SunburstFragment extends BaseFragment<SunBurstEvents> implements BackPressAware {
	private static final Logger LOG = LoggerFactory.getLogger(SunburstFragment.class);

	public interface SunBurstEvents {
		void rootChanged(String name);
	}

	private ImageView diagram;
	private SunburstDrawable<Node> sunburst;
	private NodeTreeWalker walker;

	private AsyncTask<Void, Void, Node> loadTreeTask;

	public SunburstFragment() {
		setDynamicResource(DYN_EventsClass, SunBurstEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		walker = new NodeTreeWalker();
		sunburst = new SunburstDrawable<>(walker, new Paints());
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_sunburst, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		diagram = (ImageView)view.findViewById(R.id.diagram);
		diagram.setOnTouchListener(new Toucher());
		setLoading(true);
	}

	private void setLoading(final boolean isLoading) {
		final SwipeRefreshLayout progress = (SwipeRefreshLayout)getView().findViewById(R.id.progress_circular);
		progress.post(new Runnable() {
			@Override public void run() {
				progress.setRefreshing(isLoading);
			}
		});
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadTreeTask = new AsyncTask<Void, Void, Node>() {
			@Override protected void onPreExecute() {
				setLoading(true);
			}
			@Override
			protected Node doInBackground(Void... params) {
				return new TreeLoader().build(new Node(Type.Root, -1));
			}

			@Override
			protected void onPostExecute(Node result) {
				setLoading(false);
				setRoot(result);
				diagram.setImageDrawable(sunburst);
			}
		};
		loadTreeTask.execute();
	}

	@Override public void onDestroy() {
		loadTreeTask.cancel(true);
		super.onDestroy();
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
	private void setRootInternal(Node root) {
		sunburst.setRoot(root);
		getActivity().supportInvalidateOptionsMenu();
		eventsListener.rootChanged(root.getLabel());
	}

	@Override public boolean onBackPressed() {
		if (hasPreviousRoot()) {
			setPreviousRoot();
			return true;
		}
		return false;
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.sunburst, menu);
	}

	@Override public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Node root = sunburst.getRoot();
		menu.findItem(R.id.action_sunburst_open).setVisible(root != null && root.type != Type.Root);
		menu.findItem(R.id.action_sunburst_ignore).setVisible(root != null && root.parent != null);
		menu.findItem(R.id.action_sunburst_ignore_reset).setVisible(!walker.ignore.isEmpty());
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_sunburst_open: {
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
			}
			case R.id.action_sunburst_ignore: {
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
			}
			case R.id.action_sunburst_ignore_reset: {
				for (Node node : walker.ignore) {
					while (node.parent != null) {
						node.parent.filteredCount = 0;
						node = node.parent;
					}
				}
				walker.ignore.clear();
				setRootInternal(sunburst.getRoot());
				return true;
			}
			default:
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

	public static SunburstFragment newInstance() {
		SunburstFragment fragment = new SunburstFragment();
		// TODO parametrize
		return fragment;
	}
}
