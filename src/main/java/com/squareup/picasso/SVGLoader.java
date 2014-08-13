package com.squareup.picasso;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.*;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.*;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.caverock.androidsvg.*;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;

class SVGLoader extends SimpleAsyncTask<Integer, Void, Drawable> {
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

	static Drawable load(Resources res, int resourceID) throws SVGParseException {
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