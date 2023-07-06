package net.twisterrob.inventory.android.categories.cache;

import javax.inject.Singleton;

import android.content.Context;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import net.twisterrob.inventory.android.content.Database;

@Module
@InstallIn(SingletonComponent.class)
public class CategoryCacheModule {

	@Singleton
	@Provides @NonNull CategoryCacheProvider provideCategoryCacheProvider(
			@ApplicationContext @NonNull Context context,
			@NonNull Database database
	) {
		return new CategoryCacheProvider(context, database);
	}

	// Not @Singleton, call provider every time to get the latest cache.
	@Provides @NonNull CategoryCache provideCategoryCache(
			@NonNull CategoryCacheProvider provider
	) {
		return provider.getCache();
	}
}
