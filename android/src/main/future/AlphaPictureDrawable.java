package net.twisterrob.inventory.android.view;

/**
 * Work around the fact that PictureDrawable ignores alpha.
 */
public class AlphaPictureDrawable extends PictureDrawable {
	private int alpha;

	public AlphaPictureDrawable(Picture pic) {
		super(pic);
		alpha = 0xFF;
	}

	@Override public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	@Override public void draw(Canvas canvas) {
		boolean needAlpha = getPicture() != null && alpha != 0xFF;
		if (needAlpha) {
			canvas.saveLayerAlpha(new RectF(getBounds()), alpha, Canvas.ALL_SAVE_FLAG);
		}
		if (alpha != 0x00) {
			super.draw(canvas);
		}
		if (needAlpha) {
			canvas.restore();
		}
	}

	@Override public Drawable mutate() {
		PictureDrawable drawable = new PictureDrawable(getPicture());
		drawable.setAlpha(alpha);
		return drawable;
	}
}
