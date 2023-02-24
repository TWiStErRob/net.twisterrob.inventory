package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.recyclerview.widget.*;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.android.utils.tools.DialogTools;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.fragment.ListListFragment.ListsEvents;
import net.twisterrob.inventory.android.view.ListAdapter;
import net.twisterrob.inventory.android.view.ListAdapter.ListItemEvents;
import net.twisterrob.inventory.android.view.*;

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

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
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
				DialogTools
						.prompt(getContext(), null, new PopupCallbacks<String>() {
							@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
							@Override public void finished(String value) {
								if (value == null) {
									return;
								}
								try {
									long id = App.db().createList(value);
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

	@Override public @NonNull View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState
	) {
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override protected void onStartLoading() {
		listController.startLoad(Intents.bundleFromItem(getArgItemID()));
	}

	private long getArgItemID() {
		return Intents.getItemFrom(requireArguments());
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
