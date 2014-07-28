package net.twisterrob.inventory.android.utils;

import java.io.*;

import com.google.android.gms.common.api.*;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.query.*;

import net.twisterrob.java.io.IOTools;

public class DriveUtils {
	public static <T extends Result> T syncResult(PendingResult<T> pending) {
		T result = pending.await();
		if (!result.getStatus().isSuccess()) {
			throw new IllegalStateException(result.getStatus().getStatusMessage());
		}
		return result;
	}

	public static Contents sync(PendingResult<ContentsResult> pending) {
		return syncResult(pending).getContents();
	}
	public static DriveFile sync(PendingResult<DriveFileResult> pending) {
		return syncResult(pending).getDriveFile();
	}
	public static DriveId sync(PendingResult<DriveIdResult> pending) {
		return syncResult(pending).getDriveId();
	}
	public static DriveFolder sync(PendingResult<DriveFolderResult> pending) {
		return syncResult(pending).getDriveFolder();
	}
	public static Metadata sync(PendingResult<MetadataResult> pending) {
		return syncResult(pending).getMetadata();
	}
	public static MetadataBuffer sync(PendingResult<MetadataBufferResult> pending) {
		return syncResult(pending).getMetadataBuffer();
	}

	public static DriveFolder getExistingFolder(GoogleApiClient client, DriveFolder in, String name) {
		DriveId driveId = lookup(client, in, name, "application/vnd.google-apps.folder");
		return driveId != null? Drive.DriveApi.getFolder(client, driveId) : null;
	}

	public static DriveId lookup(GoogleApiClient client, DriveFolder in, String name, String mime) {
		Query.Builder fileNameQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TRASHED, false));
		if (name != null) {
			fileNameQuery.addFilter(Filters.eq(SearchableField.TITLE, name));
		}
		if (mime != null) {
			fileNameQuery.addFilter(Filters.eq(SearchableField.MIME_TYPE, mime));
		}
		MetadataBuffer search = DriveUtils.sync(in.queryChildren(client, fileNameQuery.build()));
		return search.getCount() != 0? search.get(0).getDriveId() : null;
	}

	public static void putFileIntoContents(Contents contents, File file) throws IOException {
		@SuppressWarnings("resource")
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
			IOTools.copyStream(stream, contents.getOutputStream());
		} finally {
			IOTools.ignorantClose(stream);
		}
	}
}
