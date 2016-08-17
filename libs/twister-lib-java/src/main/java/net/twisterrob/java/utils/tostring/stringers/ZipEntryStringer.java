package net.twisterrob.java.utils.tostring.stringers;

import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

import net.twisterrob.java.utils.tostring.*;

public class ZipEntryStringer extends Stringer<ZipEntry> {
	@Override public void toString(@Nonnull ToStringAppender append, ZipEntry entry) {
		append.identity(null, entry.getName());
		append.rawProperty("time", entry.getTime());
		append.rawProperty("size", entry.getSize());
		append.rawProperty("method", method(entry.getMethod()));
		append.rawProperty("compressed", entry.getCompressedSize());
		append.formattedProperty("CRC", null, "0x%08x", entry.getCrc());
		append.complexProperty("extra", entry.getExtra());
		append.rawProperty("comment", entry.getComment());
	}

	private static String method(int method) {
		switch (method) {
			case ZipEntry.DEFLATED:
				return "DEFLATED";
			case ZipEntry.STORED:
				return "STORED";
			default:
				return "<unspecificed>";
		}
	}
}
