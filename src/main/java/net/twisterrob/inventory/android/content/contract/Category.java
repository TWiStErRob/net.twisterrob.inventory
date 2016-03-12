package net.twisterrob.inventory.android.content.contract;

import java.util.*;

public interface Category extends CommonColumns {
	String TABLE = "Category";

	long INTERNAL = -1;
	long DEFAULT = 0;

	String PARENT_ID = "parent";
	String PARENT_NAME = "parentName";
	/** No suggestions for these */
	Set<String> SKIP_SUGGEST = new HashSet<>(Arrays.asList("category_group", "category_image"));
}
