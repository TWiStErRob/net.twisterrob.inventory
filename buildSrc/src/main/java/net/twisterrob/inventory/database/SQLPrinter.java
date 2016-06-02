package net.twisterrob.inventory.database;

import java.io.*;
import java.util.Locale;

/*
INSERT INTO Category
	           (parent,  _id, name,                                      image)
	      SELECT   NULL,    -1, 'category_internal', 'category_unknown'
	UNION SELECT   NULL,     0, 'category_uncategorized',                  'category_unknown'
;
*/
public class SQLPrinter implements Printer {
	public static final String HEADER =
			"INSERT INTO Category\n\t           (parent,   _id, name,                                      image)\n";
	public static final String FOOTER = ";\n";
	private static final String SELECT = "SELECT %5s, %5s, %-42s %s";

	private Category prev;

	@Override public void start(Writer output) throws IOException {
		output.write("-- The following INSERT INTOs are generated via 'gradlew generateDB'\n\n");
	}

	@Override public void print(Category c, Writer output) throws IOException {
		String name = "'" + c.name + "',";
		String icon = "'" + c.icon + "'";
		String parent = c.parent == null? "NULL" : String.valueOf(c.parent.id);
		String id = String.valueOf(c.id);
		String debug = ""; // " -- " + c.level;
		String union;
		if (c.level == 0 && (1000 <= c.id || prev == null)) {
			if (prev != null) {
				output.write(FOOTER);
			}
			output.write(HEADER);
			union = "     ";
		} else {
			if (c.level == 1) {
				output.write("\n");
			} else if (1 < c.level) {
				name = new String(new char[c.level - 1]).replace("\0", "    ") + name;
			}
			union = "UNION";
		}
		output.write(String.format(Locale.ROOT, "\t%s " + SELECT + "%s\n", union, parent, id, name, icon, debug));
		prev = c;
	}

	@Override public void finish(Writer output) throws IOException {
		if (prev != null) {
			output.write(FOOTER);
		}
	}
}
