package net.twisterrob.inventory.android.view.adapters;

import net.twisterrob.inventory.android.content.model.ImagedDTO;

public interface GalleryEvents extends RecyclerViewItemEvents {
	void onTypeClick(int position, ImagedDTO dto);
}
