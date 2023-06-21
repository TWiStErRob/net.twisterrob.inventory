package net.twisterrob.inventory.android.backup.exporters;

import java.util.concurrent.CancellationException;

import javax.inject.Inject;

import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.backup.*;

public class BackupParcelExporter {
	private final @NonNull BackupStreamExporter exporter;

	@Inject
	public BackupParcelExporter(
			@NonNull BackupStreamExporter exporter
	) {
		this.exporter = exporter;
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
