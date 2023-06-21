package net.twisterrob.inventory.android.components;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;

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
	static @NonNull AssetManager provideAssetManager(@ApplicationContext @NonNull Context context) {
		return context.getAssets();
	}

	@Provides
	static @NonNull ContentResolver provideContentResolver(@ApplicationContext @NonNull Context context) {
		return context.getContentResolver();
	}
}
