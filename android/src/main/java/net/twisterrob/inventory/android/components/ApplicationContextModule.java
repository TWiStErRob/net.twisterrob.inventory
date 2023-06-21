package net.twisterrob.inventory.android.components;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
abstract class ApplicationContextModule {
	@Provides
	@ApplicationContext
	static @NonNull Resources provideResources(@ApplicationContext @NonNull Context context) {
		return context.getResources();
	}

	@Provides
	@ApplicationContext
	static @NonNull AssetManager provideAssetManager(@ApplicationContext @NonNull Context context) {
		return context.getAssets();
	}

	@Provides
	@ApplicationContext
	static @NonNull ContentResolver provideContentResolver(@ApplicationContext @NonNull Context context) {
		return context.getContentResolver();
	}
}
