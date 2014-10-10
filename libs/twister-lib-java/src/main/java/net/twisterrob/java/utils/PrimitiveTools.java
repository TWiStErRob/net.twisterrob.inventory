package net.twisterrob.java.utils;

public class PrimitiveTools {

	/**
	 * Mimicking static {@link Integer} methods.
	 *
	 * @see Integer#getInteger(String), but ignore the Properties part
	 */
	public static Integer parseInteger(String value) {
		try {
			return Integer.decode(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
