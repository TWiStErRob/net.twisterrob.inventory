package net.twisterrob.inventory.database;

import java.io.*;
import java.util.*;

public class StructurePrinter implements Printer {
	@Override public void start(Writer output) throws IOException {
		// no op
	}
	@Override public void print(Category c, Writer output) throws IOException {
		char[] indent = new char[c.level + 1];
		Arrays.fill(indent, '\t');
		int width = LevelBasedID.MAX_LEVEL + 1;
		String idString = c.id == Category.INVALID_ID? "INVALID" : String.format(Locale.ROOT, "%" + width + "d", c.id);
		String mark = c.id % 10 >= 8 || c.id % 100 / 10 >= 8 || c.id % 1000 / 100 >= 8? "*" : " ";
		output.write(String.format(Locale.ROOT, "%d%s / %s%s%s, icon='%s'\n",
				c.level, mark, idString, new String(indent), c.name, c.icon));
	}
	@Override public void finish(Writer output) throws IOException {
		// no op
	}
}
