package net.twisterrob.inventory.android.content;

import android.graphics.Color;
import android.support.annotation.*;

import net.twisterrob.inventory.android.R;

public interface Constants {
	String TEST_PROPERTY = "Test Property";
	String TEST_PROPERTY_OTHER = TEST_PROPERTY + " Other";
	@StringRes int TEST_PROPERTY_TYPE = R.string.property_house;
	@StringRes int TEST_PROPERTY_TYPE_OTHER = R.string.property_storage;
	String TEST_ROOM = "Test Room";
	String TEST_ROOM_OTHER = TEST_ROOM + " Other";
	String TEST_ITEM = "Test Item";
	String TEST_ITEM_OTHER = TEST_ITEM + " Other";
	String TEST_SUBITEM = "Test Sub-item";
	String TEST_SUBITEM_OTHER = TEST_SUBITEM + " Other";

	String NO_DESCRIPTION = null;
	String TEST_DESCRIPTION = "Test Description\nÁÉÓÖŐÚÜŰ\nماهو الاسم؟";
	String TEST_DESCRIPTION_OTHER = "Test Description Other";

	@ColorInt int TEST_IMAGE_COLOR = Color.YELLOW;
	@ColorInt int TEST_IMAGE_COLOR_OTHER = Color.CYAN;
}
