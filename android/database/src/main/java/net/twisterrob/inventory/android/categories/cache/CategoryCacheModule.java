package net.twisterrob.inventory.android.categories.cache;

import javax.inject.Singleton;

import android.content.Context;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class CategoryCacheModule {

	@Singleton
	@Provides @NonNull CategoryCacheProvider provideCategoryCacheProvider(
			@ApplicationContext @NonNull Context context
	) {
		return new CategoryCacheProvider(context);
	}

	// Not @Singleton, call provider every time to get the latest cache.
	@Provides @NonNull CategoryCache provideCategoryCache(
			@NonNull CategoryCacheProvider provider
	) {
		return provider.getCache();
	}
}
