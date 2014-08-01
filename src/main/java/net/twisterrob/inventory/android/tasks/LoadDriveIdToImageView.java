package net.twisterrob.inventory.android.tasks;

import java.io.InputStream;

import org.slf4j.*;

import android.graphics.*;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;

import com.google.android.gms.drive.*;

import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.utils.DriveUtils;

public class LoadDriveIdToImageView extends ApiClientAsyncTask<DriveId, Void, Bitmap> {
	private static final Logger LOG = LoggerFactory.getLogger(LoadDriveIdToImageView.class);

	private final ImageView imageView;

	public LoadDriveIdToImageView(ImageView imageView) {
		super(imageView.getContext());
		this.imageView = imageView;
	}

	@SuppressWarnings("resource")
	@Override
	protected Bitmap doInBackgroundConnected(DriveId... params) {
		if (params == null || params.length != 1 || params[0] == null) {
			return null;
		} else {
			DriveId id = params[0];
			LOG.debug("Loading {} into {}.{}", id, imageView.getContext().getClass().getSimpleName(), imageView.getId());
			DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), id);
			Contents contents = DriveUtils
					.sync(file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null));
			try {
				InputStream stream = contents.getInputStream();
				try {
					Options options = new Options();
					return BitmapFactory.decodeStream(stream, null, options);
				} finally {
					IOTools.ignorantClose(stream);
				}
			} finally {
				DriveUtils.sync(file.discardContents(getGoogleApiClient(), contents));
			}
		}
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null) {
			imageView.setImageBitmap(result);
		} else {
			LOG.error("Not bitmap loaded");
		}
	}
}