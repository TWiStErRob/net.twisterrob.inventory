package net.twisterrob.inventory.android.utils.glide;

import java.io.InputStream;

import org.slf4j.*;

import android.content.Context;
import android.graphics.drawable.*;
import android.support.v4.app.Fragment;

import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.*;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.request.RequestListener;
import com.caverock.androidsvg.*;
import com.google.android.gms.drive.DriveId;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.utils.drive.DriveIdModelLoader;
import net.twisterrob.inventory.android.utils.svg.*;

public class ImageLoaderFacade {
	private static final Logger LOG = LoggerFactory.getLogger(ImageLoaderFacade.class);

	private ImageLoaderFacade() {
		// prevent instantiation
	}

	private static class Singleton {
		static final ImageLoaderFacade INSTANCE = new ImageLoaderFacade();
	}

	public static ImageLoaderFacade getInstance() {
		return Singleton.INSTANCE;
	}

	public DrawableRequestBuilder<Integer> loadDrawable(Context context, int drawable) {
		return Glide.with(context).load(drawable) //
				.placeholder(R.drawable.image_loading) //
				.error(R.drawable.image_error) //
				.animate(android.R.anim.fade_in) //
				.listener(new LoggingListener<Integer, GlideDrawable>("Drawable"));
	}

	public DrawableRequestBuilder<DriveId> loadDrive(Fragment fragment, DriveId image) {
		return loadDriveBitmap(Glide.with(fragment), image, null);
	}

	public DrawableRequestBuilder<DriveId> loadDrive(Context context, DriveId image) {
		return loadDriveBitmap(Glide.with(context), image, null);
	}

	public DrawableRequestBuilder<DriveId> loadDrive(Context context, DriveId image,
			RequestListener<DriveId, GlideDrawable> callback) {
		return loadDriveBitmap(Glide.with(context), image, callback);
	}

	private static DrawableRequestBuilder<DriveId> loadDriveBitmap(RequestManager rm, DriveId image,
			RequestListener<DriveId, GlideDrawable> callback) {
		@SuppressWarnings("unchecked")
		MultiRequestListener<DriveId, GlideDrawable> listener = new MultiRequestListener<DriveId, GlideDrawable>(
				new LoggingListener<DriveId, GlideDrawable>("DriveId"), callback);
		return rm //
				.using(new DriveIdModelLoader()) //
				.load(image) //
				.error(R.drawable.image_drive_error) //
				.animate(android.R.anim.fade_in) //
				.listener(listener);
	}

	public GenericRequestBuilder<?, ?, ?, PictureDrawable> loadSVG(Context context, int rawResourceId) {
		@SuppressWarnings("unchecked")
		MultiRequestListener<Integer, PictureDrawable> listener = new MultiRequestListener<Integer, PictureDrawable>(
				new LoggingListener<Integer, PictureDrawable>("SVG"),
				new SoftwareLayerSetter<Integer, PictureDrawable>());
		return Glide.with(context).using(Glide.buildStreamModelLoader(Integer.class, context), InputStream.class) //
				.load(rawResourceId) //
				.as(SVG.class) //
				.transcode(new SvgDrawableTranscoder(), PictureDrawable.class) //
				.diskCacheStrategy(DiskCacheStrategy.NONE) //
				// SVG cannot be serialized so it's not worth to cache it
				// and the getResources() should be fast enough when acquiring the InputStream
				.decoder(new SvgDecoder()) //
				.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder())) // not used
				.sourceEncoder(NullEncoder.<InputStream> get()) // not used
				.encoder(NullResourceEncoder.<SVG> get()) // not used
				.placeholder(R.drawable.image_loading) //
				.error(R.drawable.image_error) //
				.animate(android.R.anim.fade_in) //
				.listener(listener) //
		;
	}

	public Drawable getSVG(Context context, int rawResourceId) {
		try {
			SVG svg = SVG.getFromResource(context, rawResourceId);
			return new PictureDrawable(svg.renderToPicture());
		} catch (SVGParseException ex) {
			LOG.warn("Cannot decode SVG from {}", rawResourceId, ex);
			return null;
		}
	}

	public void clearCaches() {
		Glide.get(App.getAppContext()).clearMemory();
	}
}
