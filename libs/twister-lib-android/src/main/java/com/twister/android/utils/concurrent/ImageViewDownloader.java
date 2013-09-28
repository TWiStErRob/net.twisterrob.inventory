package com.twister.android.utils.concurrent;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.twister.android.utils.log.*;
import com.twister.android.utils.tools.IOTools;

public class ImageViewDownloader extends AsyncTask<String, Void, Bitmap> {
	private static final Log LOG = LogFactory.getLog(Tag.IO);
	private final ImageView m_view;
	private Callback<ImageView> m_callback;

	public ImageViewDownloader(ImageView view, Callback<ImageView> callback) {
		m_view = view;
		m_callback = callback;
	}

	protected Bitmap doInBackground(String... urls) {
		String url = urls[0];
		Bitmap bitmap = null;
		try {
			bitmap = IOTools.getImage(url, true);
		} catch (Exception ex) {
			LOG.error("Cannot download %s", ex, url);
		}
		return bitmap;
	}

	protected void onPostExecute(Bitmap result) {
		m_view.setImageBitmap(result);
		if (m_callback != null) {
			m_callback.call(m_view);
		}
	}
}
