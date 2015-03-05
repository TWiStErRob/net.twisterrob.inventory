package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.*;
import android.view.View.*;
import android.widget.TextView;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.*;

public class MainFragment extends BaseFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(MainFragment.class);

	private RecyclerViewLoadersController propertiesController;
	private RecyclerViewLoadersController roomsController;
	private RecyclerViewLoadersController listsController;
	private RecyclerViewLoadersController recentsController;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		propertiesController = new RecyclerViewLoadersController(this, Loaders.Properties) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.PropertyAdapter adapter = new MainFragment.PropertyAdapter(new RecyclerViewItemEvents() {
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
		propertiesController.setView((RecyclerView)view.findViewById(R.id.properties));

		roomsController = new RecyclerViewLoadersController(this, Loaders.Rooms) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.RoomAdapter adapter = new MainFragment.RoomAdapter(new RecyclerViewItemEvents() {
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
		roomsController.setView((RecyclerView)view.findViewById(R.id.rooms));

		listsController = new RecyclerViewLoadersController(this, Loaders.Lists) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				MainFragment.ListAdapter adapter = new MainFragment.ListAdapter(null, new RecyclerViewItemEvents() {
					@Override public void onItemClick(RecyclerView.ViewHolder holder) {
						getActivity().startActivity(ListItemsActivity.show(holder.getItemId()));
					}
					@Override public boolean onItemLongClick(RecyclerView.ViewHolder holder) {
						return false;
					}
				});
				list.setAdapter(adapter);
				return adapter;
			}
		};
		listsController.setView((RecyclerView)view.findViewById(R.id.lists));

		recentsController = new RecyclerViewLoadersController(this, Loaders.Recents) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				CursorRecyclerAdapter adapter = new CursorRecyclerAdapter(null) {
					@Override public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
					}
					@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
						return new ViewHolder(parent) {
						};
					}
				};
				list.setAdapter(adapter);
				return adapter;
			}
		};
		recentsController.setView((RecyclerView)view.findViewById(R.id.recents));
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		propertiesController.startLoad(null);
		roomsController.startLoad(null);
		listsController.startLoad(null);
		recentsController.startLoad(null);
	}

	@Override public void onResume() {
		super.onResume();
		if (!getLoaderManager().hasRunningLoaders()) {
			propertiesController.refresh();
			roomsController.refresh();
			listsController.refresh();
			recentsController.refresh();
		}
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(getActivity(), menu, R.id.search);
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
						listener.onItemClick(ViewHolder.this);
					}
				});
				view.setOnLongClickListener(new OnLongClickListener() {
					@Override public boolean onLongClick(View v) {
						listener.onItemLongClick(ViewHolder.this);
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
