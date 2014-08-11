package net.twisterrob.inventory.android.utils;

import java.io.*;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.*;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.*;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.view.View;
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
		return new SVGLoader(context, resourceID);
	}

	public static class SVGLoader extends SimpleAsyncTask<Integer, Void, Drawable> {
		@SuppressWarnings("hiding")
		private static final Logger LOG = LoggerFactory.getLogger(SVGLoader.class);

		private static final LruCache<Integer, Drawable> CACHE = new LruCache<Integer, Drawable>(100);
		private static final Drawable PENDING = new PictureDrawable(null) {
			@Override
			public String toString() {
				return "PENDING";
			}
		};

		private final Resources res;
		private final int resourceID;
		private ImageView imageView;

		public SVGLoader(Context context, int resourceID) {
			this.res = context.getResources();
			this.resourceID = resourceID;
		}

		public void into(ImageView imageView) {
			this.imageView = imageView;
			Drawable drawable = CACHE.get(resourceID);
			if (drawable != null && drawable != PENDING) {
				onPostExecute(drawable);
			} else {
				execute(resourceID);
			}
		}

		@Override
		protected Drawable doInBackground(Integer resourceID) {
			try {
				//LOG.trace("Starting {} into {}", res.getResourceName(resourceID), getTarget());
				Drawable drawable;
				synchronized (CACHE) {
					drawable = CACHE.get(resourceID);
					if (drawable == null) {
						CACHE.put(resourceID, PENDING);
					}
				}
				if (drawable == PENDING) {
					return block(resourceID);
				} else if (drawable != null) {
					//LOG.trace("Cached {} into {}", res.getResourceName(resourceID), getTarget());
					return drawable;
				}
				LOG.trace("Loading SVG {} triggered by {}", res.getResourceName(resourceID), getTarget());
				drawable = load(res, resourceID);
				synchronized (PENDING) {
					//LOG.trace("Wakey {} into {}", res.getResourceName(resourceID), getTarget());
					PENDING.notifyAll();
				}
				return drawable;
			} catch (SVGParseException ex) {
				try {
					LOG.error("Cannot load {} into {}", res.getResourceName(resourceID), getTarget(), ex);
				} catch (NotFoundException nfe) {
					LOG.error(nfe.toString(), ex);
				}
			}
			return null;
		}

		private static Drawable load(Resources res, int resourceID) throws SVGParseException {
			SVG svg = SVG.getFromResource(res, resourceID);
			Drawable drawable = new PictureDrawable(svg.renderToPicture());
			CACHE.put(resourceID, drawable);
			return drawable;
		}

		private Drawable block(Integer resourceID) {
			Drawable cached;
			while ((cached = CACHE.get(resourceID)) == PENDING) {
				synchronized (PENDING) {
					//LOG.trace("Waiting for {} into {}", res.getResourceName(resourceID), getTarget());
					try {
						PENDING.wait();
					} catch (InterruptedException e) {
						Thread.interrupted();
						return null;
					}
				}
			}
			//LOG.trace("Getting out {} into {}", res.getResourceName(resourceID), getTarget());
			return cached;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			//LOG.trace("{} into {}: {}", res.getResourceName(resourceID), getTarget(), result);
			if (result == null) {
				return;
			}
			fixImageView();
			imageView.setImageDrawable(result);
		}

		private String getTarget() {
			return res.getResourceName(imageView.getId()) + "@" + imageView.hashCode();
		}

		@SuppressLint({"NewApi", "InlinedApi"})
		private void fixImageView() {
			if (android.os.Build.VERSION_CODES.HONEYCOMB <= android.os.Build.VERSION.SDK_INT) {
				imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
	}

	public Drawable getSVG(int rawResourceID) {
		try {
			return SVGLoader.load(context.getResources(), rawResourceID);
		} catch (SVGParseException ex) {
			LOG.warn("Cannot load SVG directly", ex);
			return null;
		}
	}
}
