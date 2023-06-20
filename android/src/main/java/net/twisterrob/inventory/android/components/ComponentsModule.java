package net.twisterrob.inventory.android.components;

import android.content.Context;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Database;

@Module
@InstallIn(SingletonComponent.class)
abstract class ComponentsModule {
	@Binds
	abstract @NonNull ErrorMapper bindErrorMapper(@NonNull ErrorMapperImpl impl);

	@Provides
	static @NonNull Database provideDatabase() {
		return App.db();
	}

	@Provides
	static @NonNull ResourcePreferences providePrefs() {
		return App.prefs();
	}

	@Provides
	static @NonNull Glide provideGlide(@ApplicationContext @NonNull Context context) {
		return Glide.get(context);
	}

	@Provides
	static @NonNull Toaster provideToaster() {
		return new Toaster() {
			@Override public void toast(@NonNull CharSequence message) {
				App.toast(message);
			}
		};
	}
}
