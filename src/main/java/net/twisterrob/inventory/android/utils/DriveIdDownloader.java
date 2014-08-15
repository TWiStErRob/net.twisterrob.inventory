package net.twisterrob.inventory.android.utils;

import java.io.IOException;

import org.slf4j.*;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;
import com.squareup.picasso.*;

import net.twisterrob.inventory.android.App;

import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class DriveIdDownloader implements Downloader {
	private static final Logger LOG = LoggerFactory.getLogger(DriveIdDownloader.class);

	public interface ApiClientProvider {
		GoogleApiClient getConnectedClient();
	}

	private final Downloader fallback;

	public DriveIdDownloader(Context context) {
		this(context, new UrlConnectionDownloader(context));
	}

	public DriveIdDownloader(@SuppressWarnings("unused") Context context, Downloader fallback) {
		this.fallback = fallback;
	}

	public Response load(Uri uri, boolean localCacheOnly) throws IOException {
		LOG.trace("Loading {} (localCacheOnly={})", uri, localCacheOnly);
		try {
			DriveId id = fromUri(uri);
			return loadDriveIdUrl(id, localCacheOnly);
		} catch (RuntimeException ex) {
			String fallbackMessage = fallback != null? " falling back to " + fallback : "";
			LOG.warn("Cannot load DriveId image from Uri: '{}'{}", uri, fallbackMessage, ex);
			if (fallback != null) {
				return fallback.load(uri, localCacheOnly);
			}
		}
		return null;
	}

	private static Response loadDriveIdUrl(DriveId id, boolean localCacheOnly) {
		GoogleApiClient client = App.getConnectedClient();
		if (client == null) {
			LOG.warn("No connected client received, giving custom error image");
			return null;
		}
		return download(client, id, localCacheOnly);
	}

	private static Response download(GoogleApiClient client, DriveId id, boolean localCacheOnly) {
		DriveFile file = Drive.DriveApi.getFile(client, id);
		Metadata meta = sync(file.getMetadata(client));
		boolean localFile = meta.getContentAvailability() == Metadata.CONTENT_AVAILABLE_LOCALLY;
		if (!localCacheOnly) {
			Contents contents = sync(file.openContents(client, DriveFile.MODE_READ_ONLY, null));
			try {
				return new Response(contents.getInputStream(), localFile, meta.getFileSize());
			} finally {
				sync(file.discardContents(client, contents));
			}
		} else {
			return null;
		}
	}

	public static Uri toUri(DriveId driveId) {
		return driveId == null? null : new Uri.Builder() //
				.scheme("DriveId") //
				.authority("com.google.android.apps.docs") //
				.path(driveId.encodeToString().substring("DriveId:".length())) //
				.build();
	}

	public static DriveId fromUri(Uri uri) {
		return DriveId.decodeFromString("DriveId:" + uri.getPath().substring(1));
	}
}
