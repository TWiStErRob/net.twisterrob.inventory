package net.twisterrob.inventory.android.backup;

import java.io.Serializable;
import java.util.*;

import android.support.annotation.NonNull;

public final class Progress implements Cloneable, Serializable /* poor man's parcelable */ {

	private static final long serialVersionUID = 1L;

	public final Type type;
	public Phase phase;
	/** number of images done from total */
	public int imagesDone;
	/** total number of images, may not be consistent until import finishes */
	public int imagesTotal;
	/** number of items done from total */
	public int done;
	/** total number of items */
	public int total;
	public boolean pending;
	public Throwable failure;
	public List<String> warnings = new ArrayList<>();

	public Progress(@NonNull Type type) {
		this.type = type;
		phase = Phase.Init;
		pending = true;
	}

	public Progress(@NonNull Type type, @NonNull Throwable ex) {
		this(type);
		phase = Phase.Finished;
		failure = ex;
	}

	@Override public Progress clone() {
		try {
			return (Progress)super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new InternalError(ex.toString());
		}
	}

	@Override public String toString() {
		return toString(false);
	}
	public String toString(boolean detailed) {
		String header = String.format(Locale.ROOT, "%s: data=%d/%d images=%d/%d, %spending, %d warnings, %s",
				phase, done, total, imagesDone, imagesTotal,
				pending? "" : "not ", warnings.size(), failure != null? failure : "no error");
		if (!detailed || warnings.isEmpty()) {
			return header;
		}
		StringBuilder sb = new StringBuilder(header);
		sb.append(":");
		for (String warning : warnings) {
			sb.append('\n').append(warning);
		}
		return sb.toString();
	}

	public enum Phase {
		Init,
		Data,
		Images,
		Finished
	}

	public enum Type {
		Import,
		Export
	}
}
