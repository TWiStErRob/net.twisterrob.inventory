package net.twisterrob.android.utils.tools;

import java.io.*;

import com.google.android.gms.common.api.*;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.DriveApi.*;
import com.google.android.gms.drive.DriveFolder.*;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.query.*;

import net.twisterrob.java.io.IOTools;

public class DriveTools {
	public static class ContentsUtils {
		public static DriveContents sync(PendingResult<DriveContentsResult> pending) {
			return syncResult(pending).getDriveContents();
		}

		public static void putToFile(DriveContents contents, File file) throws IOException {
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

	public static class FileUtils {
		public static DriveFile sync(PendingResult<DriveFileResult> pending) {
			return syncResult(pending).getDriveFile();
		}
	}

	public static class IdUtils {
		public static DriveId sync(PendingResult<DriveIdResult> pending) {
			return syncResult(pending).getDriveId();
		}

		public static DriveId lookup(GoogleApiClient client, DriveFolder in, String name, String mime) {
			Query.Builder fileNameQuery = new Query.Builder().addFilter(Filters.eq(SearchableField.TRASHED, false));
			if (name != null) {
				fileNameQuery.addFilter(Filters.eq(SearchableField.TITLE, name));
			}
			if (mime != null) {
				fileNameQuery.addFilter(Filters.eq(SearchableField.MIME_TYPE, mime));
			}
			MetadataBuffer search = MetaBufferUtils.sync(in.queryChildren(client, fileNameQuery.build()));
			return search.getCount() != 0? search.get(0).getDriveId() : null;
		}
	}

	public static class FolderUtils {
		public static DriveFolder sync(PendingResult<DriveFolderResult> pending) {
			return syncResult(pending).getDriveFolder();
		}

		public static DriveFolder getExisting(GoogleApiClient client, DriveFolder in, String name) {
			DriveId driveId = IdUtils.lookup(client, in, name, "application/vnd.google-apps.folder");
			return driveId != null? Drive.DriveApi.getFolder(client, driveId) : null;
		}

		public static void dump(GoogleApiClient client, DriveFolder folder) {
			MetadataBuffer children = MetaBufferUtils.sync(folder.listChildren(client));
			for (Metadata child : children) {
				DriveId childId = child.getDriveId();
				System.out.printf("%s / %s (%s) shared:%b\n",
						childId.getResourceId(), childId.encodeToString(), child.getTitle(), child.isShared());
			}
			children.close();
		}
	}

	public static class MetaDataUtils {
		public static Metadata sync(PendingResult<MetadataResult> pending) {
			return syncResult(pending).getMetadata();
		}
	}

	public static class MetaBufferUtils {
		public static MetadataBuffer sync(PendingResult<MetadataBufferResult> pending) {
			return syncResult(pending).getMetadataBuffer();
		}
	}

	public static class StatusUtils {
		public static Status sync(PendingResult<Status> pending) {
			return syncResult(pending).getStatus();
		}

		public static <T extends Result> void assertSuccess(T result) {
			if (!result.getStatus().isSuccess()) {
				throw new IllegalStateException(result.getStatus().toString());
			}
		}
	}

	public static <T extends Result> T syncResult(PendingResult<T> pending) {
		T result = pending.await();
		StatusUtils.assertSuccess(result);
		return result;
	}
}
