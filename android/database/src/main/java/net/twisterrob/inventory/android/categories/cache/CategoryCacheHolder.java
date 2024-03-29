package net.twisterrob.inventory.android.categories.cache;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.StrictMode;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.PreconditionsKt;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.contract.ParentColumns;

@Singleton
public class CategoryCacheHolder {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryCacheHolder.class);

	private @Nullable CategoryCache CACHE;
	private @Nullable Locale lastLocale;

	private final @NonNull Context context;
	private final @NonNull Database database;

	@Inject
	public CategoryCacheHolder(
			@ApplicationContext @NonNull Context context,
			@NonNull Database database
	) {
		this.context = context;
		this.database = database;
	}

	@WorkerThread
	synchronized
	private @NonNull CategoryCache get(@NonNull Context context) {
		Locale currentLocale = AndroidTools.getLocale(context.getResources().getConfiguration());
		if (!currentLocale.equals(lastLocale)) {
			// TODO externalize this to an explicit call.
			LOG.info("Locale changed from {} to {}", lastLocale, currentLocale);
			CategoryCacheImpl cache = new CategoryCacheImpl(context);
			fillItemCategories(cache);
			CACHE = cache;
			lastLocale = currentLocale;
		}
		return PreconditionsKt.checkNotNull(CACHE);
	}

	@SuppressLint("WrongThread")
	@AnyThread
	public @NonNull CategoryCache getCacheForCurrentLocale() {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			// Initialization will happen only once, after that it's cached.
			return get(context.getApplicationContext());
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	private void fillPropertyTypes(@NonNull CategoryCacheImpl cache) {
		fillCategoriesFrom(cache, database.listPropertyTypes());
	}

	private void fillRoomTypes(@NonNull CategoryCacheImpl cache) {
		fillCategoriesFrom(cache, database.listRoomTypes());
	}

	private void fillItemCategories(@NonNull CategoryCacheImpl cache) {
		fillCategoriesFrom(cache, database.listRelatedCategories(null));
	}

	private static void fillCategoriesFrom(@NonNull CategoryCacheImpl cache, @NonNull Cursor cursor) {
		try {
			while (cursor.moveToNext()) {
				String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
				long categoryID = cursor.getLong(cursor.getColumnIndexOrThrow(CommonColumns.ID));
				Long parentID = DatabaseTools.getOptionalLong(cursor, ParentColumns.PARENT_ID);
				String categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.TYPE_IMAGE));
				cache.addCategory(categoryName, categoryID, parentID, categoryIcon);
			}
		} finally {
			cursor.close();
		}
	}

	@Module
	// Installing in Activity means it cannot be injected in the Singleton scope,
	// so whenever a CategoryCache is injected it'll be a short-lived Activity/Fragment or lower.
	@InstallIn(ActivityComponent.class)
	public static class CategoryCacheModule {

		// Not scoped, call provider every time to get the latest cache.
		@Provides @NonNull CategoryCache provideCategoryCache(
				@NonNull CategoryCacheHolder provider
		) {
			return provider.getCacheForCurrentLocale();
		}
	}
}
