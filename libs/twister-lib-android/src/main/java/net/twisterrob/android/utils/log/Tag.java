package net.twisterrob.android.utils.log;

import android.util.Log;

/**
 * Available tag strings in the application logs.
 *
 * @author Zoltán Kiss
 */
@Deprecated
public enum Tag {
	/*
	 * Bear in mind that the length of tags cannot be longer than 23 characters according to the Android API.
	 */
	JSON,
	DB,
	ACCESS,
	UI,
	GEO,
	SYSTEM,
	IO;

	private static final int TAG_MAX_LENGTH = 23;

	private static final String TAG_PREFIX = "london.";

	static {
		/*
		 * Check tag string lengths
		 */
		for (Tag tag : Tag.values()) {
			if (tag.getTag().length() > TAG_MAX_LENGTH) {
				Log.w(null, String.format("Tag value is longer than 23 chars: %s=%s", tag.name(), tag.getTag()));
			}
		}
	}

	private final String m_tag;

	Tag() {
		this(null);
	}

	Tag(final String tag) {
		m_tag = TAG_PREFIX + (tag == null? name() : tag);
	}

	public String getTag() {
		return m_tag;
	}

}
