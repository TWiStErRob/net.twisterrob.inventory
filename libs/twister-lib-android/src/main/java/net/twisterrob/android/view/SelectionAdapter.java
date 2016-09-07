package net.twisterrob.android.view;

import java.util.*;

import android.annotation.TargetApi;
import android.os.Build.*;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.SparseBooleanArray;

import net.twisterrob.android.adapter.WrappingAdapter;

// CONSIDER override mDataObserver to adjust selectedItems and excludedItems indices when items move
// Changing a single item's category in the category's list keeps selection, but selects different items.
// based on http://www.grokkingandroid.com/statelistdrawables-for-recyclerview-selection/
public class SelectionAdapter<VH extends RecyclerView.ViewHolder> extends WrappingAdapter<VH> {
	private final SparseBooleanArray selectedItems = new SparseBooleanArray();
	private final SparseBooleanArray excludedItems = new SparseBooleanArray();

	public SelectionAdapter(Adapter<VH> wrapped) {
		super(wrapped);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	@Override public void onBindViewHolder(VH holder, int position) {
		boolean selected = isSelected(position);
		if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
			holder.itemView.setSelected(selected);
			holder.itemView.setActivated(selected);
		} else {
			holder.itemView.setSelected(selected);
		}
		super.onBindViewHolder(holder, position);
	}

	public void resetSelectable() {
		excludedItems.clear();
	}

	public void setSelectable(int position, boolean isSelectable) {
		if (!isSelectable) {
			excludedItems.put(position, true);
		} else {
			excludedItems.delete(position);
			setSelected(position, false);
		}
	}

	public boolean isSelectable(int position) {
		return !excludedItems.get(position);
	}

	public boolean isSelected(int position) {
		return selectedItems.get(position);
	}

	public void setSelected(int position, boolean isSelected) {
		if (!isSelectable(position)) {
			return;
		}
		if (isSelected) {
			selectedItems.put(position, true);
		} else {
			selectedItems.delete(position);
		}
	}

	public void toggleSelection(int position) {
		if (!isSelectable(position)) {
			return;
		}
		setSelected(position, !isSelected(position));
		notifyItemChanged(position);
	}

	public void selectRange(int positionStart, int itemCount) {
		selectedItems.clear();
		for (int position = positionStart; position < positionStart + itemCount; position++) {
			if (isSelectable(position)) {
				selectedItems.append(position, true);
			}
		}
		notifyItemRangeChanged(positionStart, itemCount);
	}

	public void clearSelections() {
		selectedItems.clear();
		notifyDataSetChanged();
	}

	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	public @NonNull Collection<Integer> getSelectedPositions() {
		List<Integer> items = new ArrayList<>(selectedItems.size());
		for (int i = 0; i < selectedItems.size(); i++) {
			int key = selectedItems.keyAt(i);
			assert selectedItems.get(key) : "selected items contains a 'false' element";
			items.add(key);
		}
		return items;
	}

	public void setSelectedItems(@NonNull Collection<Integer> positions) {
		selectedItems.clear();
		for (int position : positions) {
			if (isSelectable(position)) {
				selectedItems.put(position, true);
			}
		}
		notifyDataSetChanged();
	}
}
