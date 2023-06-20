package net.twisterrob.inventory.android.components;

import android.content.Context;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.BaseComponent;
import net.twisterrob.inventory.android.content.Database;

@Module
@InstallIn(SingletonComponent.class)
abstract class ComponentsModule {
	@Binds
	abstract ErrorMapper bindErrorMapper(ErrorMapperImpl impl);

	@Provides
	static BaseComponent provideBaseComponent(@ApplicationContext Context context) {
		return BaseComponent.get(context);
	}
	
	@Provides
	static Database provideDatabase() {
		return App.db();
	}
}
