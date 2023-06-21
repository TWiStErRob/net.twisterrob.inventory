package net.twisterrob.inventory.android.backup.exporters;

import javax.inject.Scope;

import androidx.annotation.NonNull;
import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.hilt.DefineComponent;
import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ServiceComponent;

import net.twisterrob.inventory.android.backup.Exporter;
import net.twisterrob.inventory.android.backup.ProgressDispatcher;
import net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter;

@DefineComponent(parent = ServiceComponent.class)
@ExportComponent.ExportScoped
public interface ExportComponent {
	@DefineComponent.Builder
	interface Builder {
		@BindsInstance
		Builder progress(ProgressDispatcher progress);
		ExportComponent build();
	}

	@Scope
	@interface ExportScoped {
	}

	@EntryPoint
	@InstallIn(ExportComponent.class)
	interface ExportEntryPoint {
		BackupParcelExporter parcelExporter();
		BackupUriExporter uriExporter();

		class Companion {
			// TODO Extension val in Kotlin.
			public static @NonNull ExportComponent.ExportEntryPoint get(
					@NonNull ExportComponent.Builder builder) {
				return EntryPoints.get(
						builder.build(),
						ExportComponent.ExportEntryPoint.class
				);
			}
		}
	}

	@Module
	@InstallIn(ExportComponent.class)
	interface HiltModule {
		@Binds
		@NonNull Exporter bindExporter(@NonNull ZippedXMLExporter impl);
	}
}
