package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.*;
import android.view.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.AndroidTools.PopupCallbacks;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.fragment.ListListFragment.ListsEvents;
import net.twisterrob.inventory.android.view.*;
import net.twisterrob.inventory.android.view.ListAdapter.ListItemEvents;

public class ListListFragment extends BaseFragment<ListsEvents> implements ListItemEvents {
	private static final Logger LOG = LoggerFactory.getLogger(ListListFragment.class);

	private RecyclerViewLoadersController listController;

	public interface ListsEvents {
		void listSelected(long listID);
		void listRemoved(long listID);
	}

	public ListListFragment() {
		setDynamicResource(DYN_EventsClass, ListsEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewLoadersController(this, Loaders.Lists) {
			@Override protected @NonNull CursorRecyclerAdapter<?> setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				ListAdapter cursorAdapter = new ListAdapter(null, ListListFragment.this);
				list.setAdapter(cursorAdapter);
				return cursorAdapter;
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
									long id = App.db().createList(value); // FIXME DB on UI
									eventsListener.listSelected(id);
								} catch (Exception ex) {
									LOG.warn("Cannot create list {}", value, ex);
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
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override protected void onStartLoading() {
		listController.startLoad(Intents.bundleFromItem(getArgItemID()));
	}

	private long getArgItemID() {
		return Intents.getItemFrom(getArguments());
	}

	@Override protected void onRefresh() {
		listController.refresh();
	}

	@Override public void addToList(long listID) {
		eventsListener.listSelected(listID);
	}

	@Override public void removeFromList(long listID) {
		eventsListener.listRemoved(listID);
	}

	public static ListListFragment newInstance(long itemID) {
		ListListFragment fragment = new ListListFragment();
		fragment.setArguments(Intents.bundleFromItem(itemID));
		return fragment;
	}
}
