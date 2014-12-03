package net.twisterrob.inventory.android.fragment;

import java.io.File;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.BaseActivity;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Room;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.IconedItem.IntentLauncher;

public class MainFragment extends BaseFragment<Void> implements BackupPickerListener {
	private static final Logger LOG = LoggerFactory.getLogger(MainFragment.class);

	private static final String BACKUP_FRAGMENT = BackupFragment.class.getSimpleName();

	private RecyclerViewLoadersController propertiesController;
	private RecyclerViewLoadersController roomsController;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			getChildFragmentManager().beginTransaction()
			                         .add(new BackupFragment(), BACKUP_FRAGMENT)
			                         .commit()
			;
		}
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		GridView list = (GridView)view.findViewById(R.id.items).findViewById(android.R.id.list);
		list.setAdapter(new IconedItemAdapter(getContext(), R.layout.item_main_nav, BaseActivity.createActions()));
		list.setOnItemClickListener(new IntentLauncher(getActivity()));

		propertiesController = new RecyclerViewLoadersController(this, Loaders.Properties) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				PropertyAdapter adapter = new PropertyAdapter(new RecyclerViewItemEvents() {
					@Override public void onItemClick(RecyclerView.ViewHolder holder) {
						getActivity().startActivity(PropertyViewActivity.show(holder.getItemId()));
					}
					@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
						return false;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
		};
		propertiesController.setView(view.findViewById(R.id.properties));

		roomsController = new RecyclerViewLoadersController(this, Loaders.Rooms) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				RoomAdapter adapter = new RoomAdapter(new RecyclerViewItemEvents() {
					@Override public void onItemClick(RecyclerView.ViewHolder holder) {
						getActivity().startActivity(RoomViewActivity.show(holder.getItemId()));
					}
					@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
						return false;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
		};
		roomsController.setView(view.findViewById(R.id.rooms));
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		propertiesController.startLoad(null);
		roomsController.startLoad(null);
	}

	@Override public void onResume() {
		super.onResume();
		if (!getLoaderManager().hasRunningLoaders()) {
			propertiesController.refresh();
			roomsController.refresh();
		}
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(getActivity(), menu, R.id.search);
	}

	public void filePicked(File file) {
		BackupFragment backup = (BackupFragment)getChildFragmentManager().findFragmentByTag(BACKUP_FRAGMENT);
		backup.filePicked(file);
	}

	static class PropertyAdapter extends BaseImagedAdapter {
		public PropertyAdapter(RecyclerViewItemEvents listener) {
			super(null, R.layout.item_main_property, listener);
		}
	}

	static class RoomAdapter extends BaseImagedAdapter<RoomAdapter.ViewHolder> {
		public RoomAdapter(RecyclerViewItemEvents listener) {
			super(null, listener);
		}

		class ViewHolder extends BaseImagedAdapter.ViewHolder {
			public ViewHolder(View view) {
				super(view);
				details = (TextView)view.findViewById(R.id.details);
			}

			final TextView details;
		}

		@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ViewHolder(inflateView(parent, R.layout.item_main_room));
		}

		@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
			super.onBindViewHolder(holder, cursor);
			String property = cursor.getString(cursor.getColumnIndexOrThrow(Room.PROPERTY_NAME));
			holder.details.setText(holder.details.getContext().getString(R.string.room_location, property));
		}
	}

	public static MainFragment newInstance() {
		MainFragment fragment = new MainFragment();
		return fragment;
	}
}
