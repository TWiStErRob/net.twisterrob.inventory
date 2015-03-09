package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.EditText;

import net.twisterrob.android.adapter.CursorRecyclerAdapter;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.ExtrasFactory;
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

	private HeaderManager header = null;

	public ListListFragment() {
		setDynamicResource(DYN_EventsClass, ListsEvents.class);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new RecyclerViewLoadersController(this, Loaders.Lists) {
			@Override protected CursorRecyclerAdapter setupList() {
				list.setLayoutManager(new LinearLayoutManager(getContext()));
				ListAdapter cursorAdapter = new ListAdapter(null, ListListFragment.this);
				list.setAdapter(cursorAdapter);
				return cursorAdapter;
			}

			@Override public boolean canCreateNew() {
				return true;
			}

			@Override protected void onCreateNew() {
				final EditText input = new EditText(getContext());
				new AlertDialog.Builder(getContext())
						.setTitle("New List")
						.setMessage("Please enter a name for the list!")
						.setView(input)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String value = input.getText().toString();
								long id = App.db().createList(value); // FIXME DB on UI
								eventsListener.listSelected(id);
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.generic_list, container, false);
	}

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listController.setView((RecyclerView)view.findViewById(android.R.id.list));
	}

	@Override
	protected void onStartLoading() {
		listController.startLoad(ExtrasFactory.bundleFromItem(getArgItemID()));
	}

	private long getArgItemID() {
		return ExtrasFactory.getItemFrom(getArguments());
	}

	@Override
	protected void onRefresh() {
		if (header != null) {
			header.getHeader().refresh();
		}
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
		fragment.setArguments(ExtrasFactory.bundleFromItem(itemID));
		return fragment;
	}
}
