package net.twisterrob.inventory.android.utils;

import java.io.InputStream;

import org.slf4j.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.support.v4.app.Fragment;
import android.util.TypedValue;

import static android.util.TypedValue.*;

import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.*;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.request.RequestListener;
import com.caverock.androidsvg.*;
import com.google.android.gms.drive.DriveId;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.inventory.android.*;

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
				.using(new DriveIdModelLoader(null)) // FIXME App.getConnectedClient()
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
		// SVG cannot be serialized so it's not worth to cache it
		// and the getResources() should be fast enough when acquiring the InputStream
		return Glide.with(context)
		            .using(Glide.buildStreamModelLoader(Integer.class, context), InputStream.class)
		            .load(rawResourceId)
		            .as(SVG.class)
		            .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
		            .diskCacheStrategy(DiskCacheStrategy.NONE)
		            .decoder(new SvgDecoder())
		            .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()) /* not used */)
		            .sourceEncoder(NullEncoder.<InputStream>get() /* not used */)
		            .encoder(NullResourceEncoder.<SVG>get() /* not used */)
		            .placeholder(R.drawable.image_loading)
		            .error(R.drawable.image_error)
		            .animate(android.R.anim.fade_in)
		            .listener(listener)
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
	public Drawable getSVG(Context context, int rawResourceId, int size, int padding) {
		try {
			float dp = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
			SVG svg = SVG.getFromResource(context, rawResourceId);
			Picture picture = new Picture();
			Canvas canvas = picture.beginRecording((int)(size * dp), (int)(size * dp));
			canvas.translate(padding * dp, padding * dp); // workaround, because renderToCanvas doesn't care about x,y
			svg.renderToCanvas(canvas, new RectF(0, 0, (size - 2 * padding) * dp, (size - 2 * padding) * dp));
			picture.endRecording();
			return new PictureDrawable(picture);
		} catch (SVGParseException ex) {
			LOG.warn("Cannot decode SVG from {}", rawResourceId, ex);
			return null;
		}
	}

	public void clearCaches() {
		Glide.get(App.getAppContext()).clearMemory();
	}
}
