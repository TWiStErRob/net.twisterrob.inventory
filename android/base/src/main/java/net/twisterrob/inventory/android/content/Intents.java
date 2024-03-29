package net.twisterrob.inventory.android.content;

import java.io.Serializable;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.*;

import net.twisterrob.inventory.android.content.contract.*;

@SuppressWarnings("SameParameterValue")
public final class Intents {
	public interface Extras {
		String PROPERTY_ID = "propertyID";
		String ROOM_ID = "roomID";
		String PARENT_ID = "parentID";
		String ITEM_ID = "itemID";
		String CATEGORY_ID = "categoryID";
		String LIST_ID = "listID";
		String INCLUDE_SUBS = "includeSubs";

		String PARENT_EQUALS_BACK = "navigation:parent==back";
	}

	public static @NonNull Intent childNav(@NonNull Intent intent) {
		intent.putExtra(Extras.PARENT_EQUALS_BACK, true);
		return intent;
	}
	public static boolean isChildNav(@NonNull Intent intent) {
		return intent.getBooleanExtra(Extras.PARENT_EQUALS_BACK, false);
	}

	//region Bundle Factories
	public static @NonNull Bundle bundleFrom(@NonNull String key, @Nullable Serializable value) {
		Bundle args = new Bundle();
		args.putSerializable(key, value);
		return args;
	}

	public static @NonNull Bundle bundleFromProperty(long propertyID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.PROPERTY_ID, propertyID);
		return bundle;
	}
	public static @NonNull Bundle bundleFromRoom(long roomID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.ROOM_ID, roomID);
		return bundle;
	}
	public static @NonNull Bundle bundleFromParent(long itemID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.PARENT_ID, itemID);
		return bundle;
	}
	public static @NonNull Bundle bundleFromItem(long itemID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.ITEM_ID, itemID);
		return bundle;
	}
	public static @NonNull Bundle bundleFromCategory(long categoryID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.CATEGORY_ID, categoryID);
		return bundle;
	}
	public static @NonNull Bundle bundleFromList(long listID) {
		Bundle bundle = new Bundle();
		bundle.putLong(Extras.LIST_ID, listID);
		return bundle;
	}
	public static @NonNull Bundle bundleFromIDs(long... IDs) {
		Bundle bundle = new Bundle();
		bundle.putLongArray("IDs", IDs);
		return bundle;
	}
	public static @NonNull Bundle bundleFromQuery(CharSequence query) {
		Bundle args = new Bundle();
		args.putCharSequence(SearchManager.QUERY, query);
		return args;
	}
	//endregion Bundle Factories

	//region Intent Factories
	public static @NonNull Intent intentFromProperty(long propertyID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromProperty(propertyID));
		return intent;
	}
	public static @NonNull Intent intentFromRoom(long roomID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromRoom(roomID));
		return intent;
	}
	public static @NonNull Intent intentFromParent(long itemID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromParent(itemID));
		return intent;
	}
	public static @NonNull Intent intentFromItem(long itemID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromItem(itemID));
		return intent;
	}
	public static @NonNull Intent intentFromCategory(long categoryID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromCategory(categoryID));
		return intent;
	}
	public static @NonNull Intent intentFromList(long listID) {
		Intent intent = new Intent();
		intent.putExtras(bundleFromList(listID));
		return intent;
	}
	//endregion Intent Factories

	//region Getters
	public static long[] getIDsFrom(@NonNull Bundle bundle) {
		return bundle.getLongArray("IDs");
	}
	public static long getItemFrom(@NonNull Intent intent) {
		return getItemFrom(intent.getExtras());
	}
	public static long getPropertyFrom(@Nullable Bundle bundle) {
		return bundle != null? bundle.getLong(Extras.PROPERTY_ID, Property.ID_ADD) : Property.ID_ADD;
	}
	public static long getItemFrom(@Nullable Bundle bundle) {
		return bundle != null? bundle.getLong(Extras.ITEM_ID, Item.ID_ADD) : Item.ID_ADD;
	}
	public static long getCategory(@Nullable Bundle bundle) {
		return bundle != null? bundle.getLong(Extras.CATEGORY_ID, Category.ID_ADD) : Category.ID_ADD;
	}

	/**
	 * Helper that supports:
	 * <ul>
	 *     <li>{@link Intent#putExtra(String, Serializable)}</li>
	 *     <li>{@link Bundle#putSerializable(String, Serializable)}</li>
	 *     <li>{@link Intent#putExtra(String, long)}</li>
	 *     <li>{@link Bundle#putLong(String, long)}</li>
	 * </ul>
	 */
	@SuppressWarnings("deprecation") // We don't know the type.
	public static @Nullable Long getOptionalCategory(@NonNull Bundle bundle) {
		return (Long)bundle.get(Extras.CATEGORY_ID);
	}
	//endregion Getters

	private Intents() {
		// prevent instantiation
	}
}
