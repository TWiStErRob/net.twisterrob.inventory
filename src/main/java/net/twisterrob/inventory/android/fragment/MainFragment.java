package net.twisterrob.inventory.android.fragment;

import java.text.*;
import java.util.*;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.*;
import android.text.format.DateUtils;
import android.view.*;
import android.view.View.*;
import android.widget.TextView;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.AndroidTools.PopupCallbacks;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.ListItemsActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;
import net.twisterrob.inventory.android.fragment.data.RoomListFragment.RoomsEvents;
import net.twisterrob.inventory.android.view.RecyclerViewLoadersController;
import net.twisterrob.inventory.android.view.adapters.*;

public class MainFragment extends BaseFragment<MainFragment.MainEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(MainFragment.class);

	public interface MainEvents extends PropertiesEvents, RoomsEvents, ItemsEvents {
	}

	private RecyclerViewLoadersController propertiesController;
	private RecyclerViewLoadersController roomsController;
	private RecyclerViewLoadersController listsController;
	private RecyclerViewLoadersController recentsController;

	public MainFragment() {
		setDynamicResource(DYN_EventsClass, MainEvents.class);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		propertiesController = new RecyclerViewLoadersController(this, Loaders.Properties) {
			@Override protected @NonNull CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.PropertyAdapter adapter = new MainFragment.PropertyAdapter(new RecyclerViewItemEvents() {
					@Override public void onItemClick(int position, long recyclerViewItemID) {
						eventsListener.propertySelected(recyclerViewItemID);
					}
					@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
						eventsListener.propertyActioned(recyclerViewItemID);
						return true;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
			@Override public boolean canCreateNew() {
				return true;
			}
			@Override protected void onCreateNew() {
				eventsListener.newProperty();
			}
		};
		propertiesController.setView((RecyclerView)view.findViewById(R.id.properties));

		roomsController = new RecyclerViewLoadersController(this, Loaders.Rooms) {
			@Override protected @NonNull CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.RoomAdapter adapter = new MainFragment.RoomAdapter(new RecyclerViewItemEvents() {
					@Override public void onItemClick(int position, long recyclerViewItemID) {
						eventsListener.roomSelected(recyclerViewItemID);
					}
					@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
						eventsListener.roomActioned(recyclerViewItemID);
						return false;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
		};
		roomsController.setView((RecyclerView)view.findViewById(R.id.rooms));

		listsController = new RecyclerViewLoadersController(this, Loaders.Lists) {
			@Override protected @NonNull CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.ListAdapter adapter = new MainFragment.ListAdapter(null, new RecyclerViewItemEvents() {
					@Override public void onItemClick(int position, long recyclerViewItemID) {
						getActivity().startActivity(ListItemsActivity.show(recyclerViewItemID));
					}
					@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
						return false;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
			@Override public boolean canCreateNew() {
				return true;
			}

			@Override protected void onCreateNew() {
				AndroidTools
						.prompt(getContext(), new PopupCallbacks<String>() {
							@Override public void finished(String value) {
								if (value == null) {
									return;
								}
								try {
									App.db().createList(value); // FIXME DB on UI
									listsController.refresh();
								} catch (Exception ex) {
									LOG.warn("Cannot create list '{}'", value, ex);
									App.toastUser(App.getError(ex, R.string.list_error_new, value));
								}
							}
						})
						.setTitle("New List")
						.setMessage("Please enter a name for the list!")
						.show()
				;
			}
		};
		listsController.setView((RecyclerView)view.findViewById(R.id.lists));

		recentsController = new RecyclerViewLoadersController(this, Loaders.Recents) {
			@Override protected @NonNull CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.RecentAdapter adapter = new MainFragment.RecentAdapter(new RecyclerViewItemEvents() {
					@Override public void onItemClick(int position, long recyclerViewItemID) {
						eventsListener.itemSelected(recyclerViewItemID);
					}
					@Override public boolean onItemLongClick(int position, long recyclerViewItemID) {
						// TODO make swipe delete the item
						App.db().deleteRecentsOfItem(recyclerViewItemID); // FIXME DB on UI
						recentsController.refresh();
						return true;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
		};
		recentsController.setView((RecyclerView)view.findViewById(R.id.items));
	}

	@Override protected void onStartLoading() {
		propertiesController.startLoad(null);
		roomsController.startLoad(null);
		listsController.startLoad(null);
		recentsController.startLoad(null);
	}

	@Override protected void onRefresh() {
		propertiesController.refresh();
		roomsController.refresh();
		listsController.refresh();
		recentsController.refresh();
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

	static class RecentAdapter extends BaseImagedAdapter<RecentAdapter.ViewHolder> {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

		public RecentAdapter(RecyclerViewItemEvents listener) {
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
			return new ViewHolder(inflateView(parent, R.layout.item_main_recent));
		}

		@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
			super.onBindViewHolder(holder, cursor);
			CharSequence visit = cursor.getString(cursor.getColumnIndexOrThrow("visit"));
			try {
				Date date = format.parse(visit.toString());
				visit = DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(),
						DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
			} catch (ParseException e) { /* use the original visit string*/ }
			// int vRank = cursor.getInt(cursor.getColumnIndexOrThrow("visitRank"));
			int pop = cursor.getInt(cursor.getColumnIndexOrThrow("population"));
			// float perc = cursor.getFloat(cursor.getColumnIndexOrThrow("percentage"));
			// int pRank = cursor.getInt(cursor.getColumnIndexOrThrow("populationRank"));

			holder.details.setText(String.format("%1$s (%2$dx)", visit, pop));
		}
	}

	static class ListAdapter extends CursorRecyclerAdapter<ListAdapter.ViewHolder> {
		private final RecyclerViewItemEvents listener;

		public ListAdapter(Cursor cursor, RecyclerViewItemEvents listener) {
			super(cursor);
			this.listener = listener;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View view) {
				super(view);
				title = (TextView)view.findViewById(R.id.title);
				count = (TextView)view.findViewById(R.id.count);

				view.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						listener.onItemClick(getAdapterPosition(), getItemId());
					}
				});
				view.setOnLongClickListener(new OnLongClickListener() {
					@Override public boolean onLongClick(View v) {
						listener.onItemLongClick(getAdapterPosition(), getItemId());
						return true;
					}
				});
			}

			public final TextView title;
			public final TextView count;
		}

		@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			return new ViewHolder(inflater.inflate(R.layout.item_main_list, parent, false));
		}

		@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
			String listName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
			int listCount = cursor.getInt(cursor.getColumnIndexOrThrow(CommonColumns.COUNT_CHILDREN_DIRECT));
			holder.title.setText(listName);
			holder.count.setText(String.valueOf(listCount));
		}
	}

	public static MainFragment newInstance() {
		MainFragment fragment = new MainFragment();
		return fragment;
	}
}
