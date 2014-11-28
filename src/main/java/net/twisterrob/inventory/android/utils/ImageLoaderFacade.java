package net.twisterrob.inventory.android.utils;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;

import org.slf4j.*;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.support.v4.app.Fragment;

import com.bumptech.glide.*;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.caverock.androidsvg.*;

import net.twisterrob.android.content.glide.*;
import net.twisterrob.android.content.glide.LoggingListener.ResourceFormatter;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.view.AlphaPictureDrawable;

public class ImageLoaderFacade {
	private static final Logger LOG = LoggerFactory.getLogger(ImageLoaderFacade.class);

	public static final LoggingListener<String, GlideDrawable> IMAGE_LISTENER = new LoggingListener<>("image");
	public static final LoggingListener<Integer, GlideDrawable> DRAWABLE_LISTENER = new LoggingListener<>("Drawable");
	private static final LoggingListener<Integer, PictureDrawable> SVG_LISTENER = new LoggingListener<>("SVG",
			new ResourceFormatter(App.getAppContext()));
	public static final SoftwareLayerSetter<Integer, PictureDrawable> SVG_SOFTWARE_LAYER = new SoftwareLayerSetter<>();

	private ImageLoaderFacade() {
		// prevent instantiation
	}

	private static class Singleton {
		static final ImageLoaderFacade INSTANCE = new ImageLoaderFacade();
	}

	public static ImageLoaderFacade getInstance() {
		return Singleton.INSTANCE;
	}

	public DrawableRequestBuilder<Integer> startDrawable(Context context) {
		return Glide.with(context)
		            .fromResource()
		            .placeholder(R.drawable.image_loading)
		            .error(R.drawable.image_error)
		            .animate(android.R.anim.fade_in)
		            .listener(DRAWABLE_LISTENER)
				;
	}

	public DrawableRequestBuilder<String> start(Fragment fragment) {
		return start(Glide.with(fragment), null);
	}

	public DrawableRequestBuilder<String> start(Context context) {
		return start(Glide.with(context), null);
	}

	public DrawableRequestBuilder<String> start(Context context, RequestListener<String, GlideDrawable> callback) {
		return start(Glide.with(context), callback);
	}

	private static DrawableRequestBuilder<String> start(RequestManager rm,
			RequestListener<String, GlideDrawable> callback) {
		@SuppressWarnings("unchecked")
		MultiRequestListener<String, GlideDrawable> listener = new MultiRequestListener<>(IMAGE_LISTENER, callback);
		return rm.fromString()
		         .error(R.drawable.image_error)
		         .animate(android.R.anim.fade_in)
		         .listener(listener)
				;
	}

	public GenericRequestBuilder<Integer, ?, ?, PictureDrawable> startSVG(final Context context) {
		@SuppressWarnings("unchecked")
		MultiRequestListener<Integer, PictureDrawable> listener =
				new MultiRequestListener<>(SVG_LISTENER, SVG_SOFTWARE_LAYER);
		// SVG cannot be serialized so it's not worth to cache it
		// and the getResources() should be fast enough when acquiring the InputStream
		return Glide.with(context)
		            .using(Glide.buildStreamModelLoader(Integer.class, context), InputStream.class)
		            .from(Integer.class)
		            .as(SVG.class)
		            .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
		            .decoder(new SvgDecoder())
		            .diskCacheStrategy(DiskCacheStrategy.NONE)
		            .placeholder(R.drawable.image_loading)
		            .error(R.drawable.image_error)
		            .animate(android.R.anim.fade_in)
		            .listener(listener)
				;
	}

	private final ConcurrentMap<Integer, WeakReference<Picture>> SVGs = new ConcurrentHashMap<>();
	public Drawable getSVG(Context context, int rawResourceId) {
		try {
			WeakReference<Picture> reference = SVGs.get(rawResourceId);
			Picture pic = null;
			if (reference != null) {
				pic = reference.get();
			}
			if (pic == null) {
				SVG svg = SVG.getFromResource(context, rawResourceId);
				pic = svg.renderToPicture();
				SVGs.put(rawResourceId, new WeakReference<>(pic));
			}
			return new AlphaPictureDrawable(pic);
		} catch (SVGParseException ex) {
			LOG.warn("Cannot decode SVG from {}", rawResourceId, ex);
			return null;
		}
	}
	public Drawable getSVG(Context context, int rawResourceId, int size, int padding) {
		try {
			SVG svg = SVG.getFromResource(context, rawResourceId);
			Picture picture = new Picture();
			Canvas canvas = picture.beginRecording(size, size);
			canvas.translate(padding, padding); // workaround, because renderToCanvas doesn't care about x,y
			svg.renderToCanvas(canvas, new RectF(0, 0, size - 2 * padding, size - 2 * padding));
			picture.endRecording();
			return new AlphaPictureDrawable(picture);
		} catch (SVGParseException ex) {
			LOG.warn("Cannot decode SVG from {}", rawResourceId, ex);
			return null;
		}
	}

	public void clearCaches() {
		Glide.get(App.getAppContext()).clearMemory();
	}
}
