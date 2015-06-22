package net.twisterrob.inventory.android.content.model;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.support.annotation.*;

import net.twisterrob.android.db.DatabaseOpenHelper;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.java.collections.RestoreStructureAlgo;

public class CategoryHelpBuilder {
	private static final String STYLE = "<style>\n"
			+ "h2.category {\n"
			+ "    margin: 0;\n"
			+ "    margin-top: 0.25em;\n"
			+ "}\n"
			+ "h3.subcategory {\n"
			+ "    margin: 0;\n"
			+ "    margin-top: 0.5em;\n"
			+ "}\n"
			+ ".level {\n"
			+ "    margin-left: 1em;\n"
			+ "}\n"
			+ "p.keywords {\n"
			+ "    margin-top: .25em;\n"
			+ "    margin-bottom: 0;\n"
			+ "}\n"
			+ ".description {\n"
			+ "    font-style: italic;\n"
			+ "    font-size: 0.75em;\n"
			+ "    font-weight: normal;\n"
			+ "}\n"
			+ ".description:before {\n"
			+ "    content: '\\200A\\2014\\200A'; /* x200A=hairline space, x2014=emdash */ \n"
			+ "}\n"
			+ ".keyword-group {\n"
			+ "    display: block;\n"
			+ "}\n"
			+ ".keyword-group.collapsed.has-examples {\n"
			+ "    /*text-decoration: underline;*/\n"
			+ "}\n"
			+ ".keyword-group.collapsed {\n"
			+ "    display: inline;\n"
			+ "}\n"
			+ "\n"
			+ "#legend {\n"
			+ "     font-size: small;\n"
			+ "     margin: 0;\n"
			+ "}\n"
			+ "@media print {\n"
			+ "    #legend {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "    .keyword-group.has-examples .keyword {\n"
			+ "        font-weight: bold;\n"
			+ "    }\n"
			+ "    .keyword-group.collapsed.has-examples {\n"
			+ "        display: block;\n"
			+ "    }\n"
			+ "    body {\n"
			+ "        font-size: 0.9em;\n"
			+ "    }\n"
			+ "}\n"
			+ "@media screen {\n"
			+ "    .description {\n"
			+ "        color: #888;\n"
			+ "    }\n"
			+ "    .examples {\n"
			+ "        color: #888;\n"
			+ "    }\n"
			+ "    .keyword-group.has-examples .keyword {\n"
			+ "        cursor: pointer;\n"
			+ "    }\n"
			+ "    .keyword-group.collapsed .examples {\n"
			+ "        display: none;\n"
			+ "    }\n"
			+ "    .keyword-group.collapsed.has-examples .keyword:after {\n"
			+ "        content: '*';\n"
			+ "    }\n"
			+ "}\n"
			+ "</style>\n\n";

	private static final String HEADER = ""
			+ "<h1>%s Category Keywords</h1>\n"
			+ "<p id=\"legend\">\n"
			+ "     It is advised to look at this document in landscape.\n"
			+ "     <br/>\n"
			+ "     <button onclick=\"for (var i = 0; i < window.keywords.length; ++i) { window.keywords[i].classList.remove('collapsed'); }\">Expand All</button>\n"
			+ "     <button onclick=\"for (var i = 0; i < window.keywords.length; ++i) { window.keywords[i].classList.add('collapsed'); }\">Collapse All</button>\n"
			+ "     <br/>\n"
			+ "     <span>*</span> Click individual generic keywords to collapse and expand.\n"
			+ "</p>\n\n";

	private static final String FOOTER = "<script>\n"
			+ "var keywords = document.querySelectorAll('.keyword-group.has-examples');\n"
			+ "for (var i = 0; i < keywords.length; ++i) {\n"
			+ "    keywords[i].querySelector('.keyword').onclick = function() {\n"
			+ "        this.parentElement.classList.toggle('collapsed');\n"
			+ "    }\n"
			+ "}\n"
			+ "</script>\n\n";

	private final Context context;
	public CategoryHelpBuilder(Context context) {
		this.context = context;
	}

	public void export(File file) throws IOException {
		PrintWriter out = new PrintWriter(file);
		write(out);
		AndroidTools.makeFileDiscoverable(context, file);
	}

	public String buildHTML() {
		StringWriter str = new StringWriter();
		PrintWriter out = new PrintWriter(str);
		try {
			write(out);
		} catch (IOException ex) {
			throw new IllegalStateException("Can't write StringWriter", ex);
		}
		return str.toString();
	}

	private void write(PrintWriter out) throws IOException {
		Cursor cursor = App.db().listRelatedCategories(null);
		try {
			out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
			out.write(STYLE);
			out.printf(HEADER, context.getString(R.string.app_name));
			new WriteCategoryTree(out).run(cursor);
			out.write(FOOTER);
			out.close();
			if (out.checkError()) {
				throw new IOException("Printing HTML failed.");
			}
		} finally {
			cursor.close();
		}
	}
	private void writeCat(PrintWriter out, Cursor cursor) {
		String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CommonColumns.NAME));
		Long parentID = DatabaseTools.getOptionalLong(cursor, ParentColumns.PARENT_ID);
		CharSequence categoryTitle = AndroidTools.getText(context, categoryName);
		if (parentID == null) {
			out.printf(Locale.ROOT,
					"<h2 class=\"category\" id=\"%s\">%s<span class=\"description\">%s</span></h3>\n",
					categoryName, categoryTitle, AndroidTools.getText(context, categoryName + "_description"));
		} else {
			out.printf(Locale.ROOT, "<h3 class=\"subcategory\" id=\"%s\">%s</h3>\n"
							+ "<p class=\"keywords level\">\n%s\n</p>",
					categoryName, categoryTitle, buildKeywords(categoryName));
		}
	}

	private CharSequence buildKeywords(String categoryName) {
		try {
			CharSequence keywords = AndroidTools.getText(context, categoryName + "_keywords");
			Pattern p = Pattern.compile("\\s*([^,]+?)\\s*(?:\\(([^)]+)\\))?([,;]|\\z)", Pattern.MULTILINE);
			Matcher m = p.matcher(keywords);

			StringBuffer builder = new StringBuffer();
			while (m.find()) {
				if (m.end(2) - m.start(2) == 0) {
					m.appendReplacement(builder,
							"<span class=\"keyword-group collapsed\"><span class=\"keyword\">$1</span>$3 </span>\n");
				} else {
					m.appendReplacement(builder,
							"<span class=\"keyword-group has-examples\"><span class=\"keyword\">$1</span><span class=\"examples\"> ($2)</span>$3 </span>\n");
				}
			}
			m.appendTail(builder);
			return builder.toString();
		} catch (NotFoundException ex) {
			return String.format(Locale.ROOT, "<span class=\"error\">Keywords not found for %s</span>", categoryName);
		}
	}

	private class WriteCategoryTree extends RestoreStructureAlgo<Cursor, Cursor, Void> {
		private final PrintWriter out;
		WriteCategoryTree(PrintWriter out) {
			this.out = out;
		}

		@Override protected @NonNull Iterator<Cursor> start(@NonNull Cursor data) {
			data.moveToPosition(-1);
			return DatabaseTools.iterate(data).iterator();
		}
		@Override protected int getLevel(Cursor cursor) {
			int level = 0;
			if (cursor != null) {
				int levelColumn = cursor.getColumnIndex("level");
				if (levelColumn != DatabaseOpenHelper.CURSOR_NO_COLUMN) {
					level = cursor.getInt(levelColumn);
				}
			}
			return level;
		}
		@Override protected void onIncrementLevel(int level, @Nullable Cursor cursor) {
			out.print("<div class=\"level\">\n");
		}
		@Override protected void onEntity(int level, @NonNull Cursor cursor) {
			writeCat(out, cursor);
		}
		@Override protected void onDecrementLevel(int level) {
			out.print("</div>\n");
		}
		@Override protected Void finish() {
			return null;
		}
	}
}

