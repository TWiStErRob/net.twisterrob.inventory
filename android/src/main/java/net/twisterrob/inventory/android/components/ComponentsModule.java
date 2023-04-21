package net.twisterrob.inventory.android.components;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
interface ComponentsModule {
	@Binds ErrorMapper bindErrorMapper(ErrorMapperImpl impl);
}
