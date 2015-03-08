private static GenericRequestBuilder<Integer, ?, ?, PictureDrawable> startSVG(final Context context) {
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
	            .listener(new SoftwareLayerSetter<Integer, PictureDrawable>())
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
