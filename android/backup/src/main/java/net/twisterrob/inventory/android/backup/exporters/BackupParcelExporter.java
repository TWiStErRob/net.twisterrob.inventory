package net.twisterrob.inventory.android.backup.exporters;

import java.util.concurrent.CancellationException;

import javax.inject.Inject;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import dagger.hilt.android.qualifiers.ApplicationContext;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;

public class BackupParcelExporter {
	private final BackupStreamExporter exporter;

	@VisibleForTesting BackupParcelExporter(
			@NonNull BackupStreamExporter exporter
	) {
		this.exporter = exporter;
	}
	
	@Inject
	public BackupParcelExporter(
			@ApplicationContext @NonNull Context context,
			@NonNull Exporter exporter,
			@NonNull ProgressDispatcher dispatcher
	) {
		this(new BackupStreamExporter(exporter, DBProvider.db(context), dispatcher));
	}

	public Progress exportTo(ParcelFileDescriptor file) {
		AutoCloseOutputStream output = new AutoCloseOutputStream(file);
		try {
			Progress progress = exporter.export(output);
			if (IOTools.isEPIPE(progress.failure)) {
				Exception newFailure = new CancellationException("external app cancelled");
				newFailure.initCause(progress.failure);
				progress.failure = newFailure;
			}
			return progress;
		} finally {
			IOTools.ignorantClose(output);
		}
	}
}
