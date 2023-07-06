package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.content.Intents;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ListDTO;
import net.twisterrob.inventory.android.fragment.ListViewFragment.ListEvents;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.Dialogs;

import static net.twisterrob.inventory.android.content.Loaders.*;

@AndroidEntryPoint
public class ListViewFragment extends BaseSingleLoaderFragment<ListEvents> {
	private static final Logger LOG = LoggerFactory.getLogger(ListViewFragment.class);

	public interface ListEvents {
		void listLoaded(ListDTO list);
		void listDeleted(ListDTO list);
	}

	public ListViewFragment() {
		setDynamicResource(DYN_EventsClass, ListEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.list);
	}

	@Override public boolean hasUI() {
		return false;
	}

	@Override protected void onRefresh() {
		super.onRefresh();
		getLoaderManager().getLoader(SingleList.id()).onContentChanged();
	}

	@Override protected void onStartLoading() {
		super.onStartLoading();
		getLoaderManager().initLoader(SingleList.id(),
				Intents.bundleFromList(getArgListID()),
				SingleList.createCallbacks(requireContext(), new SingleRowLoaded())
		);
	}

	@Override protected void onSingleRowLoaded(@NonNull Cursor cursor) {
		ListDTO list = ListDTO.fromCursor(cursor);
		eventsListener.listLoaded(list);
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_list_edit: {
				rename(getArgListID());
				return true;
			}
			case R.id.action_list_delete: {
				delete(getArgListID());
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void rename(long listID) {
		Dialogs.executeConfirm(requireActivity(), new RenameListAction(listID) {
			@Override public void finished() {
				refresh();
			}

			@Override public void undoFinished() {
				// no undo, because confirmed
			}
		});
	}

	private void delete(final long listID) {
		Dialogs.executeConfirm(requireActivity(), new DeleteListAction(listID) {
			@Override public void finished() {
				ListDTO list = new ListDTO();
				list.id = listID;
				eventsListener.listDeleted(list);
			}
		});
	}

	private long getArgListID() {
		return requireArguments().getLong(Extras.LIST_ID, CommonColumns.ID_ADD);
	}

	public static ListViewFragment newInstance(long listID) {
		ListViewFragment fragment = new ListViewFragment();
		fragment.setArguments(Intents.bundleFromList(listID));
		return fragment;
	}
}
