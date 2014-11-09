package net.twisterrob.android.content.glide;

import java.io.InputStream;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveId;

public class DriveIdModelLoader implements StreamModelLoader<DriveId> {
	private final GoogleApiClient client;

	public DriveIdModelLoader(GoogleApiClient client) {
		this.client = client;
	}

	public DataFetcher<InputStream> getResourceFetcher(DriveId model, int width, int height) {
		return new DriveIdDataFetcher(client, model);
	}
}
