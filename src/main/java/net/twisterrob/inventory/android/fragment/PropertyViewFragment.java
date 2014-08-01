package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.PropertyDTO;
import net.twisterrob.inventory.android.fragment.PropertyViewFragment.PropertyEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertyTask;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class PropertyViewFragment extends BaseViewFragment<PropertyEvents> {
	static final Logger LOG = LoggerFactory.getLogger(PropertyViewFragment.class);

	public interface PropertyEvents {
		void propertyLoaded(PropertyDTO property);
	}

	public PropertyViewFragment() {
		setDynamicResource(DYN_EventsClass, PropertyEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.property);
	}

	private TextView propertyName;
	private TextView propertyType;
	private ImageView propertyImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.property_view, container, false);
		propertyName = (TextView)root.findViewById(R.id.propertyName);
		propertyType = (TextView)root.findViewById(R.id.propertyType);
		propertyImage = (ImageView)root.findViewById(R.id.propertyImage);
		return root;
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, getArgPropertyID());
		getLoaderManager().initLoader(SingleProperty.ordinal(), args, new LoadExistingProperty());
	}

	public void refresh() {
		getLoaderManager().getLoader(SingleProperty.ordinal()).forceLoad();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_edit:
				startActivity(PropertyEditActivity.edit(getArgPropertyID()));
				return true;
			case R.id.action_property_delete:
				Dialogs.executeTask(getActivity(), new DeletePropertyTask(getArgPropertyID(), new Dialogs.Callback() {
					public void success() {
						getActivity().finish();
					}
					public void failed() {
						String message = "This property still has some rooms";
						Toast.makeText(App.getAppContext(), message, Toast.LENGTH_LONG).show();
					}
				}));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private long getArgPropertyID() {
		return getArguments().getLong(Extras.PROPERTY_ID, Property.ID_ADD);
	}

	private final class LoadExistingProperty extends LoadSingleRow {
		private LoadExistingProperty() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor cursor) {
			super.process(cursor);
			PropertyDTO property = PropertyDTO.fromCursor(cursor);

			getActivity().setTitle(property.name);
			propertyName.setText(property.name);
			propertyType.setText(String.valueOf(property.type));

			int propertyTypeResourceID = property.getImageResourceID(getActivity());
			App.pic().load(property.imageDriveID) //
					.placeholder(propertyTypeResourceID) //
					.error(propertyTypeResourceID) //
					.into(propertyImage);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}

	public static PropertyViewFragment newInstance(long propertyID) {
		if (propertyID == Property.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing property");
		}

		PropertyViewFragment fragment = new PropertyViewFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PROPERTY_ID, propertyID);

		fragment.setArguments(args);
		return fragment;
	}
}
