package net.twisterrob.inventory.android.sunburst;

import java.util.Stack;

import org.slf4j.*;

import android.graphics.Matrix;
import android.os.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import net.twisterrob.android.activity.BackPressAware;
import net.twisterrob.android.graphics.SunburstDrawable;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
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

	private AsyncTask<Void, Void, Node> loadTreeTask;

	public SunburstFragment() {
		setDynamicResource(DYN_EventsClass, SunBurstEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sunburst = new SunburstDrawable<>(new NodeTreeWalker(), new Paints());
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_sunburst, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		diagram = (ImageView)view.findViewById(R.id.diagram);
		diagram.setOnTouchListener(new Toucher());
		diagram.setImageResource(R.drawable.image_loading);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadTreeTask = new AsyncTask<Void, Void, Node>() {
			@Override
			protected Node doInBackground(Void... params) {
				Node root = new Node(Type.Root, -1, null);
				new TreeLoader().build(root);
				return root;
			}

			@Override
			protected void onPostExecute(Node result) {
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
		if (root == null || sunburst.getRoot() == root) {
			return;
		}
		if (sunburst.getRoot() != null) {
			stack.add(sunburst.getRoot());
		}
		sunburst.setHighlighted(null);
		sunburst.setRoot(root);
		eventsListener.rootChanged(root.getLabel());
	}

	private boolean hasPreviousRoot() {
		return !stack.isEmpty();
	}

	private void setPreviousRoot() {
		Node root = stack.pop();
		sunburst.setHighlighted(sunburst.getRoot());
		sunburst.setRoot(root);
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
		menu.add(0, 1, 0, "Open");
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				Node root = sunburst.getRoot();
				if (root != null) {
					startActivity(ItemViewActivity.show(root.id));
					return true;
				}
				return false;
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
