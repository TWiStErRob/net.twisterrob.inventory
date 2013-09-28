package com.twister.android.utils.tools;
public class PrimitiveTools {

	/**
	 * Mimicking static {@link Integer} methods.
	 * 
	 * @see Integer#getInteger(String)
	 */
	public static Integer parseInteger(String value) {
		try {
			return Integer.decode(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

}
