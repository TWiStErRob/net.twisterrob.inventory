package net.twisterrob.inventory.android.components;

import androidx.annotation.NonNull;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import net.twisterrob.android.content.pref.ResourcePreferences;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.Database;

@Module
@InstallIn(SingletonComponent.class)
abstract class ComponentsModule {
	@Binds
	abstract ErrorMapper bindErrorMapper(ErrorMapperImpl impl);

	@Provides
	static Database provideDatabase() {
		return App.db();
	}

	@Provides
	static ResourcePreferences providePrefs() {
		return App.prefs();
	}

	@Provides
	static @NonNull Toaster provideToaster() {
		return new ToasterImpl();
	}
}
