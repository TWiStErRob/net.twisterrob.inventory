package net.twisterrob.inventory.android.utils.drive;

import java.io.InputStream;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.google.android.gms.drive.DriveId;

public class DriveIdModelLoader implements StreamModelLoader<DriveId> {
	public DataFetcher<InputStream> getResourceFetcher(DriveId model, int width, int height) {
		return new DriveIdDataFetcher(model);
	}
}
