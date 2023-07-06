package net.twisterrob.inventory.android.fragment.data;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.appcompat.view.ActionMode;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.MainActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.fragment.data.PropertyListFragment.PropertiesEvents;
import net.twisterrob.inventory.android.tasks.DeletePropertiesAction;
import net.twisterrob.inventory.android.view.*;

@AndroidEntryPoint
public class PropertyListFragment extends BaseGalleryFragment<PropertiesEvents> {

	public interface PropertiesEvents {
		void newProperty();
		void propertySelected(long propertyID);
		void propertyActioned(long propertyID);
	}

	public PropertyListFragment() {
		setDynamicResource(DYN_EventsClass, PropertiesEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.property_list);
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listController = new BaseGalleryController(Loaders.Properties, R.string.property_empty_child) {
			@Override public boolean canCreateNew() {
				return true;
			}
			@Override protected void onCreateNew() {
				eventsListener.newProperty();
			}
		};
	}

	@Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_property_add:
				listController.createNew();
				return true;
			case R.id.action_room_list:
				startActivity(MainActivity.list(requireContext(), MainActivity.PAGE_ROOMS));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected SelectionActionMode onPrepareSelectionMode(@NonNull SelectionAdapter<?> adapter) {
		return new PropertySelectionActionMode(this, adapter);
	}

	@Override protected void onListItemClick(int position, long recyclerViewItemID) {
		eventsListener.propertySelected(recyclerViewItemID);
	}

	@Override protected void onListItemLongClick(int position, long recyclerViewItemID) {
		eventsListener.propertyActioned(recyclerViewItemID);
	}

	public static PropertyListFragment newInstance() {
		PropertyListFragment fragment = new PropertyListFragment();
		fragment.setArguments(new Bundle());
		return fragment;
	}

	private static class PropertySelectionActionMode extends SelectionActionMode {
		private final @NonNull BaseFragment<?> fragment;

		public PropertySelectionActionMode(@NonNull BaseFragment<?> fragment, @NonNull SelectionAdapter<?> adapter) {
			super(fragment.requireActivity(), adapter);
			this.fragment = fragment;
		}
		@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.property_bulk, menu);
			return super.onCreateActionMode(mode, menu);
		}

		@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.action_property_delete:
					delete(getSelectedIDs());
					return true;
			}
			return super.onActionItemClicked(mode, item);
		}

		@Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
			return false;
		}

		private void delete(final long... propertyIDs) {
			Dialogs.executeConfirm(getActivity(), new DeletePropertiesAction(propertyIDs) {
				public void finished() {
					finish();
					fragment.refresh();
				}
			});
		}
	}
}
