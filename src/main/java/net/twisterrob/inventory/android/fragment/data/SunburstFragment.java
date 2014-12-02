package net.twisterrob.inventory.android.fragment.data;

import java.util.*;

import android.database.Cursor;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import net.twisterrob.android.graphics.SunburstDrawable;
import net.twisterrob.android.graphics.SunburstDrawable.BasePaintStrategy;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.SunburstFragment.Listener;

public class SunburstFragment extends BaseFragment<Listener> {
	public interface Listener {
		void rootChanged(Node root);
	}

	private ImageView diagram;
	private SunburstDrawable<Node> sunburst;

	public SunburstFragment() {
		setDynamicResource(DYN_EventsClass, Listener.class);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		sunburst = new SunburstDrawable<>(new NodeTreeWalker(), new Paints());
		return inflater.inflate(R.layout.fragment_sunburst, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		diagram = (ImageView)view.findViewById(R.id.diagram);
		diagram.setOnTouchListener(new Toucher());
		diagram.setImageResource(R.drawable.image_loading);

		new AsyncTask<Void, Void, Node>() {
			@Override
			protected Node doInBackground(Void... params) {
				Node root = new Node(Node.Type.Root, 0, null);
				new TreeLoader().build(root);
				return root;
			}

			@Override
			protected void onPostExecute(Node result) {
				setRoot(result);
				diagram.setImageDrawable(sunburst);
			}
		}.execute();
	}

	private final Stack<Node> stack = new Stack<>();

	public void setRoot(Node root) {
		if (root == null || sunburst.getRoot() == root) {
			return;
		}
		if (sunburst.getRoot() != null) {
			stack.add(sunburst.getRoot());
		}
		sunburst.setHighlight(null);
		sunburst.setRoot(root);
		eventsListener.rootChanged(root);
	}

	public boolean hasPreviousRoot() {
		return !stack.isEmpty();
	}
	public void setPreviousRoot() {
		Node root = stack.pop();
		sunburst.setHighlight(sunburst.getRoot());
		sunburst.setRoot(root);
		eventsListener.rootChanged(root);
	}

	private class Toucher implements OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					sunburst.setHighlight(whichNode(event));
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

	private static class TreeLoader {
		private void build(Node node) {
			int count = 0;
			int depth = -1;
			List<Node> children = loadChildren(node);
			for (Node child : children) {
				build(child);
				if (depth < child.depth) {
					depth = child.depth;
				}
				count += child.count;
			}
			node.depth = depth + 1;
			node.count = children.isEmpty()? 1 : count;
		}

		public List<Node> loadChildren(Node node) {
			switch (node.type) {
				case Root:
					node.children = loadProperties();
					break;
				case Property:
					node.children = loadRooms(node);
					break;
				case Item:
				case Room:
					node.children = loadItems(node);
					break;
				default:
					throw new IllegalStateException("Unknown type: " + node.type);
			}
			return node.children;
		}

		private static List<Node> loadProperties() {
			Cursor cursor = App.db().listProperties();
			try {
				List<Node> children = new ArrayList<>();
				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex(Property.ID));
					String label = cursor.getString(cursor.getColumnIndex(Property.NAME));
					children.add(new Node(Node.Type.Property, id, label));
				}
				return children;
			} finally {
				cursor.close();
			}
		}
		private static List<Node> loadRooms(Node node) {
			Cursor cursor = App.db().listRooms(node.id);
			try {
				List<Node> children = new ArrayList<>();
				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex(Room.ID));
					String label = cursor.getString(cursor.getColumnIndex(Room.NAME));
					children.add(new Node(Node.Type.Room, id, label));
				}
				return children;
			} finally {
				cursor.close();
			}
		}
		private static List<Node> loadItems(Node node) {
			Cursor cursor;
			if (node.type == Node.Type.Room) {
				cursor = App.db().listItemsInRoom(node.id);
			} else {
				cursor = App.db().listItems(node.id);
			}
			try {
				List<Node> children = new ArrayList<>();
				while (cursor.moveToNext()) {
					long id = cursor.getLong(cursor.getColumnIndex(Item.ID));
					String label = cursor.getString(cursor.getColumnIndex(Item.NAME));
					children.add(new Node(Node.Type.Item, id, label));
				}
				return children;
			} finally {
				cursor.close();
			}
		}
	}

	public static class Node {
		private final Type type;
		private final long id;
		private final String label;
		private int count;
		private int depth;
		private List<Node> children;

		public Node(Type type, long id, String label) {
			this.type = type;
			this.id = id;
			this.label = label;
		}

		enum Type {
			Root,
			Property,
			Room,
			Item
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return String.format(Locale.ROOT, "%s #%d: %s", type, id, label);
		}
	}

	private static class NodeTreeWalker implements SunburstDrawable.TreeWalker<Node> {
		public Iterable<Node> getChildren(Node node) {
			return node.children;
		}
		public int getDepth(Node subTree) {
			return subTree.depth;
		}
		public String getLabel(Node node) {
			return node.label;
		}
		public float getWeight(Node subTree) {
			return subTree.count;
		}
	}

	private static class Paints extends BasePaintStrategy<Node> {
		private final float[] hsv = new float[] {0.0f, 0.5f, 1.0f};

		private Paint hue(Paint paint, float start, float end) {
			hsv[0] = (start + end) / 2 * 360;
			paint.setColor(Color.HSVToColor(hsv));
			return paint;
		}

		public Paint getFill(Node node, int level, float start, float end) {
			return hue(fill, start, end);
		}
		@Override
		public Paint getStroke(Node node, int level, float start, float end) {
			stroke.setARGB(128, 128, 128, 128);
			return stroke;
		}
		@Override
		public Paint getText(Node node, int level, float start, float end) {
			text.setARGB(text.getAlpha(), 0, 0, 0);
			return text;
		}
	}

	public static SunburstFragment newInstance() {
		SunburstFragment fragment = new SunburstFragment();
		// TODO parametrize
		return fragment;
	}
}
