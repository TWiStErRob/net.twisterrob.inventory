package net.twisterrob.inventory.android.categories.cache;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.BaseComponent;
import net.twisterrob.inventory.android.PreconditionsKt;
import net.twisterrob.inventory.android.content.Database;

@Singleton
public class CategoryCacheProvider {
	private static final Logger LOG = LoggerFactory.getLogger(CategoryCacheProvider.class);

	private @Nullable CategoryCache CACHE;
	private @Nullable Locale lastLocale;
	
	private final @NonNull Context context;

	//@Inject // Intentionally not possible to create it automatically, CategoryCacheModule controls lifecycle.
	public CategoryCacheProvider(@ApplicationContext @NonNull Context context) {
		this.context = context;
	}

	@WorkerThread
	synchronized
	private @NonNull CategoryCache get(@NonNull Context context) {
		Locale currentLocale = AndroidTools.getLocale(context.getResources().getConfiguration());
		if (!currentLocale.equals(lastLocale)) {
			// TODO externalize this to an explicit call.
			LOG.info("Locale changed from {} to {}", lastLocale, currentLocale);
			@SuppressWarnings("deprecation")
			Database database = (Database)BaseComponent.get(context).db();
			CACHE = new CategoryCacheImpl(database, context);
			lastLocale = currentLocale;
		}
		return PreconditionsKt.checkNotNull(CACHE);
	}

	@SuppressLint("WrongThread")
	@AnyThread
	public @NonNull CategoryCache getCache() {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			// Initialization will happen only once, after that it's cached.
			return get(context);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}
}
