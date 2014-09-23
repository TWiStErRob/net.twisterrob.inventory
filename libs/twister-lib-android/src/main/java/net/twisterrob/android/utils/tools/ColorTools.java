package net.twisterrob.android.utils.tools;

import android.graphics.Color;

public class ColorTools {
	/**
	 * Same as {@link Color#HSVToColor(int, float[])}, but without the weird interface and much faster.
	 * Values are not clamped, anything out of range may explode!
	 *
	 * @param hue        [0, 1]
	 * @param saturation [0, 1]
	 * @param brightness [0, 1]
	 * @param alpha      [0, 1]
	 * @return RGBA color
	 */
	@SuppressWarnings("incomplete-switch")
	public static int fromHsb(float hue, float saturation, float brightness, float alpha) {
		float r = 0, g = 0, b = 0;
		if (brightness == 0) { // black
			r = g = b = 0;
		} else if (saturation == 0) { // grays
			r = g = b = brightness;
		} else {
			float hueSix = hue * 6.f;
			int hueSixCategory = (int)hueSix;
			float hueSixRemainder = hueSix - hueSixCategory;
			float pv = (1.f - saturation) * brightness;
			float qv = (1.f - saturation * hueSixRemainder) * brightness;
			float tv = (1.f - saturation * (1.f - hueSixRemainder)) * brightness;
			switch (hueSixCategory) {
				case 0:
				case 6: // r
					r = brightness;
					g = tv;
					b = pv;
					break;
				case 1: // g
					r = qv;
					g = brightness;
					b = pv;
					break;
				case 2:
					r = pv;
					g = brightness;
					b = tv;
					break;
				case 3: // b
					r = pv;
					g = qv;
					b = brightness;
					break;
				case 4:
					r = tv;
					g = pv;
					b = brightness;
					break;
				case 5: // back to r
					r = brightness;
					g = pv;
					b = qv;
					break;
			}
		}
		return ((int)(alpha * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}

	public static int randomColor() {
		return fromHsb((float)Math.random(), 1, 1, 1);
	}
}
