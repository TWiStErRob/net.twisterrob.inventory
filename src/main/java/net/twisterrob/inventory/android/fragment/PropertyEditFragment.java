package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.*;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.*;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyEditFragment extends EditFragment {
	private Spinner propertyType;
	private EditText propertyName;
	private CursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.property_edit, container, false);
		propertyName = (EditText)root.findViewById(R.id.propertyName);
		propertyType = (Spinner)root.findViewById(R.id.propertyType);
		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.property_types, (Cursor)null);

		propertyType.setAdapter(adapter);
		propertyType.setOnItemSelectedListener(new DefaultValueUpdater(propertyName, Property.NAME));

		return root;
	}

	@Override
	public void edit(long id) {
		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());
		Dependency<Cursor> populateTypes = manager.add(PropertyTypes.ordinal(), null, new CursorSwapper(getActivity(),
				adapter));

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, id);

		Dependency<Cursor> loadPropertyData = manager.add(SingleProperty.ordinal(), args, new LoadExistingProperty());
		Dependency<Void> loadPropertyCondition = manager.add(-SingleProperty.ordinal(), args, new IsExistingProperty());

		populateTypes.providesResultFor(loadPropertyData.dependsOn(loadPropertyCondition));
		manager.startLoading();
	}

	private class IsExistingProperty extends DynamicLoaderManager.Condition {
		private IsExistingProperty() {
			super(getActivity());
		}

		// TODO probably can move this to Dependency graph building part
		@Override
		protected boolean test(int id, Bundle args) {
			return args != null && args.getLong(Extras.PROPERTY_ID, Property.ID_ADD) != Property.ID_ADD;
		}
	}

	private class LoadExistingProperty extends LoadSingleRow {
		LoadExistingProperty() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			String name = item.getString(item.getColumnIndexOrThrow(Property.NAME));
			long type = item.getLong(item.getColumnIndexOrThrow(Property.TYPE));

			getActivity().setTitle(name);
			propertyName.setText(name);
			AndroidTools.selectByID(propertyType, type);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}
}
