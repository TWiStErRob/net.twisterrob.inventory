package net.twisterrob.inventory.android.backup.importers;

import javax.inject.Scope;

import android.net.Uri;

import androidx.annotation.NonNull;
import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.hilt.DefineComponent;
import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;

import net.twisterrob.inventory.android.backup.ImportProgressHandler;
import net.twisterrob.inventory.android.backup.ProgressDispatcher;

@DefineComponent(parent = ServiceComponent.class)
@ImportComponent.ImportScoped
public interface ImportComponent {
	@DefineComponent.Builder
	interface Builder {
		@BindsInstance
		Builder progress(ProgressDispatcher progress);
		ImportComponent build();
	}

	@Scope
	@interface ImportScoped {
	}

	@EntryPoint
	@InstallIn(ImportComponent.class)
	interface ImportEntryPoint {
		ImportProgressHandler progress();
		BackupTransactingImporter<Uri> importer();

		class Companion {
			// TODO Extension val in Kotlin.
			public static @NonNull ImportEntryPoint get(@NonNull ImportComponent.Builder builder) {
				return EntryPoints.get(
						builder.build(),
						ImportComponent.ImportEntryPoint.class
				);
			}
		}
	}

	@Module
	@InstallIn(ImportComponent.class)
	interface HiltModule {
		@Binds
		@NonNull ZipImporter<Uri> bindImporter(BackupZipUriImporter impl);
	}
}
