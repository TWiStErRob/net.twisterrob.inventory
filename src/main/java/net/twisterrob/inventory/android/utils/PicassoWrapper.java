package net.twisterrob.inventory.android.utils;

import java.io.*;

import org.slf4j.*;

import android.content.Context;
import android.graphics.drawable.*;
import android.net.Uri;
import android.widget.ImageView;

import com.caverock.androidsvg.*;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.*;
import com.squareup.picasso.*;
import com.squareup.picasso.Picasso.Listener;
import com.squareup.picasso.Picasso.RequestTransformer;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.*;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.tasks.ApiClientAsyncTask;

import static net.twisterrob.inventory.android.utils.DriveUtils.*;

public class PicassoWrapper implements Downloader, Listener, RequestTransformer {
	private static final Logger LOG = LoggerFactory.getLogger(PicassoWrapper.class);

	private final Context context;
	private final Picasso picasso;
	private final Downloader fallback;

	public PicassoWrapper(Context context) {
		this.context = context;
		this.picasso = new Picasso.Builder(context) //
				.indicatorsEnabled(BuildConfig.DEBUG) // XXX disable on release
				.loggingEnabled(false) // XXX disable on release
				.downloader(this) //
				.listener(this) //
				.requestTransformer(this) //
				.build();
		this.fallback = new UrlConnectionDownloader(context);
	}

	public Request transformRequest(Request request) {
		//LOG.debug("Transforming request {}", request);
		return request;
	}

	public Response load(Uri uri, boolean localCacheOnly) throws IOException {
		//LOG.debug("Loading {} (localCacheOnly={})", uri, localCacheOnly);
		try {
			DriveId id = fromUri(uri);
			return loadDriveIdUrl(id, localCacheOnly);
		} catch (RuntimeException ex) {
			return fallback.load(uri, localCacheOnly);
		}
	}

	public void onImageLoadFailed(Picasso picasso, Uri uri, Exception ex) {
		LOG.error("Cannot load image with {} from {}", picasso, uri, ex);
	}

	private static Response loadDriveIdUrl(DriveId id, boolean localCacheOnly) {
		GoogleApiClient client = ApiClientAsyncTask.createConnectedClient(App.getAppContext());
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

	public RequestCreator load(DriveId driveId) {
		return picasso.load(toUri(driveId)).error(R.drawable.image_error);
	}

	public RequestCreator loadDriveId(String driveId) {
		DriveId id = driveId == null? null : DriveId.decodeFromString(driveId);
		return load(id);
	}

	public RequestCreator loadNothing() {
		return picasso.load((Uri)null);
	}

	public RequestCreator load(int resourceId) {
		return picasso.load(resourceId).error(R.drawable.image_error);
	}

	public RequestCreator load(Uri uri) {
		return picasso.load(uri).error(R.drawable.image_error);
	}

	public RequestCreator load(File file) {
		return picasso.load(file).error(R.drawable.image_error);
	}

	public Picasso getPicasso() {
		return picasso;
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

	public SVGLoader loadSVG(int resourceID) {
		return new SVGLoader(resourceID);
	}

	public class SVGLoader extends SimpleAsyncTask<Integer, Void, Drawable> {
		private final int resourceID;
		private ImageView imageView;

		public SVGLoader(int resourceID) {
			this.resourceID = resourceID;
		}

		public void into(ImageView imageView) {
			this.imageView = imageView;
			execute(resourceID);
		}

		@Override
		protected Drawable doInBackground(Integer param) {
			try {
				SVG svg = SVG.getFromResource(context, resourceID);
				return new PictureDrawable(svg.renderToPicture());
			} catch (SVGParseException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		protected void onPostExecute(Drawable result) {
			imageView.setImageDrawable(result);
		}
	}
}
