package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Property;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class PropertiesFragment extends BaseFragment {
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFragment.class);

	public interface PropertyEvents {
		void newProperty();
		void propertySelected(long propertyID);
		void propertyActioned(long propertyID);
	}

	private CursorAdapter adapter;
	private GridView grid;
	private PropertyEvents listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = checkActivityInterface(activity, PropertyEvents.class);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);
		menuInflater.inflate(R.menu.properties, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_add:
				listener.newProperty();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.property_coll, container, false);
		grid = (GridView)root.findViewById(R.id.properties);
		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.properties, (Cursor)null);

		grid.setAdapter(adapter);

		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		grid.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				listener.propertyActioned(id);
				return true;
			}
		});
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				if (id == Property.ID_ADD) {
					listener.newProperty();
				} else {
					listener.propertySelected(id);
				}
			}
		});
	}

	public void list() {
		getLoaderManager().initLoader(Loaders.Properties.ordinal(), null, new CursorSwapper(getActivity(), adapter));
	}

	public void refresh() {
		getLoaderManager().getLoader(Loaders.Properties.ordinal()).forceLoad();
	}
}
