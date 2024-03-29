package net.twisterrob.inventory.android.utils;

import android.graphics.Color;
import android.graphics.ColorMatrix;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

public abstract class PictureHelper {
	private static final float[] NEGATIVE = new float[] {
			-1, 0, 0, 0, 255,
			0, -1, 0, 0, 255,
			0, 0, -1, 0, 255,
			0, 0, 0, 1, 0
	};

	/**
	 * Given a transparent greyscale image this tints the blacks/greys with the given color, leaves white intact.
	 * <ul>
	 * <li>first negative hides the whites</li>
	 * <li>whites (originally black) are replaced to inverse of accent via a PorterDuff.MULTIPLY</li>
	 * <li>blacks (originally white) are not affected by multiply (<code>0 * c == c</code>)</li>
	 * <li>inverse of accent through another negative will become accent</li>
	 * </ul>
	 */
	public static @NonNull ColorMatrix tintMatrix(@ColorInt int color) {
		ColorMatrix matrix = new ColorMatrix();
		matrix.postConcat(new ColorMatrix(NEGATIVE));
		matrix.postConcat(new ColorMatrix(new float[] {
				1 - Color.red(color) / 255f, 0, 0, 0, 0,
				0, 1 - Color.green(color) / 255f, 0, 0, 0,
				0, 0, 1 - Color.blue(color) / 255f, 0, 0,
				0, 0, 0, Color.alpha(color) / 255f, 0
		}));
		matrix.postConcat(new ColorMatrix(NEGATIVE));
		return matrix;
	}

	public static @NonNull ColorMatrix postAlpha(
			@FloatRange(from = 0, to = 1) float alpha, @NonNull ColorMatrix matrix) {
		matrix.postConcat(new ColorMatrix(new float[] {
				1, 0, 0, 0, 0,
				0, 1, 0, 0, 0,
				0, 0, 1, 0, 0,
				0, 0, 0, alpha, 0
		}));
		return matrix;
	}

	/**
	 * @see <a href="http://stackoverflow.com/a/31217267/253468">SO</a>
	 * @see <a href="https://gist.github.com/ro-sharp/49fd46a071a267d9e5dd">Gist</a>
	 */
	public static @ColorInt int getColor(@NonNull Object thing) {
		int seed = thing.hashCode();
		// Math.sin jumps big enough even when adding 1, because argument is radian and period is ~3
		// The `& 0xFF` will truncate the higher bits, so we end up with [0, 255] range.
		int rand_r = (int)Math.abs(Math.sin(++seed) * 10000) & 0xFF;
		int rand_g = (int)Math.abs(Math.sin(++seed) * 10000) & 0xFF;
		int rand_b = (int)Math.abs(Math.sin(++seed) * 10000) & 0xFF;

		// r, g, b in range of [(160 + 0) / 2 = 80, (160 + 255) / 2 = 207]
		int r = (160 + rand_r) / 2;
		int g = (160 + rand_g) / 2;
		int b = (160 + rand_b) / 2;
		return Color.argb(0xFF, r, g, b);
	}
}
