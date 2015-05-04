package net.twisterrob.android.content.glide;

import com.bumptech.glide.signature.StringSignature;

public class LongSignature extends StringSignature {
	private final long key;

	public LongSignature() {
		this(System.currentTimeMillis());
	}

	public LongSignature(long key) {
		super(String.valueOf(key));
		if (key == 0) {
			throw new IllegalStateException("0 signature, is the some data missing?");
		}
		this.key = key;
	}

	@Override public String toString() {
		return "LongSignature(" + key + ")";
	}
}
