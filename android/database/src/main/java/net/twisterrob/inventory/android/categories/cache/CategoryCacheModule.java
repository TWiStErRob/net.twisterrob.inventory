package net.twisterrob.inventory.android.categories.cache;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class CategoryCacheModule {

	// Not @Singleton, call provider every time to get the latest cache.
	@Provides @NonNull CategoryCache provideCategoryCache(
			@NonNull CategoryCacheProvider provider
	) {
		return provider.getCache();
	}
}
