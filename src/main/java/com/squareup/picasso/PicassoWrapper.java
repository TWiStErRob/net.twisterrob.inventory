package com.squareup.picasso;

import java.io.File;

import org.slf4j.*;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.caverock.androidsvg.SVGParseException;
import com.google.android.gms.drive.DriveId;
import com.squareup.picasso.Picasso.Listener;

import net.twisterrob.inventory.*;
import net.twisterrob.inventory.android.utils.DriveIdDownloader;

public class PicassoWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(PicassoWrapper.class);

	private final Context context;
	private final Picasso picasso;

	public PicassoWrapper(Context context) {
		this.context = context;
		this.picasso = new Picasso.Builder(context) //
				.indicatorsEnabled(BuildConfig.DEBUG) // XXX disable on release
				.loggingEnabled(false) // XXX disable on release
				.downloader(new DriveIdDownloader(context)) //
				.listener(new Listener() {
					public void onImageLoadFailed(Picasso picasso, Uri uri, Exception ex) {
						LOG.error("Cannot load image with {} from {}", picasso, uri, ex);
					}
				}) //
				.build();
	}

	public Picasso getPicasso() {
		return picasso;
	}

	public static void clearCache(Picasso picasso) {
		picasso.cache.clear();
	}

	public RequestCreator load(DriveId driveId) {
		return picasso.load(DriveIdDownloader.toUri(driveId)).error(R.drawable.image_drive_error);
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

	public SVGLoader loadSVG(int resourceID) {
		return new SVGLoader(context, resourceID);
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
