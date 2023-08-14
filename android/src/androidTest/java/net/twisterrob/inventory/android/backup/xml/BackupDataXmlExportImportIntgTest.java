package net.twisterrob.inventory.android.backup.xml;

import java.io.*;

import org.junit.*;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.twisterrob.inventory.android.backup.Importer;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.Types;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.activity.TestActivity;

@RunWith(AndroidJUnit4.class)
public class BackupDataXmlExportImportIntgTest {

	private static final String BAD_XML = "<!-- --> -- & &amp; &lt; &gt; <?xml?> <![CDATA[ ]]>";

	@Rule public final InventoryActivityRule<TestActivity> activity
			= new InventoryActivityRule<>(TestActivity.class, false, false);
	@Rule public final DataBaseActor database = new AppSingletonDatabaseActor();

	private Context context;

	@Before public void setUp() {
		activity.reset();
		context = ApplicationProvider.getApplicationContext();
	}

	@Test public void testEmpty() throws Exception {
		Database db = database.getDatabase();
		database.assertHasNoUserData();

		CursorExporter exporter = new XMLExporter(db, "xslt.file", "xsd.file");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (Cursor export = db.export()) {
			exporter.start(output, export);
			exporter.finish(export);
		}

		database.assertHasNoUserData();
		Importer importer = new XMLImporter(context.getResources(), db, new Types(db));
		importer.doImport(new ByteArrayInputStream(output.toByteArray()), null, null);
		database.assertHasNoUserData();
	}

	@Test public void testXMLCommentProblems() throws Exception {
		Database db = database.getDatabase();
		long prop = db.createProperty(PropertyType.DEFAULT, bad("Property"), badDesc("Property"));
		long room = db.createRoom(prop, RoomType.DEFAULT, bad("Room"), badDesc("Room"));
		long item = db.createItem(room, Category.DEFAULT, bad("Item"), badDesc("Item"));
		long list = db.createList(bad("List"));
		db.addListEntry(list, item);

		CursorExporter exporter = new XMLExporter(db, "xslt.file", "xsd.file");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (Cursor export = db.export()) {
			exporter.start(output, export);
			while (export.moveToNext()) {
				exporter.processEntry(export);
			}
			exporter.finish(export);
		}

		db.deleteList(list);
		db.deleteProperty(prop);
		database.assertHasNoUserData();

		Importer importer = new XMLImporter(context.getResources(), db, new Types(db));
		importer.doImport(new ByteArrayInputStream(output.toByteArray()), null, null);
		database.assertPropertyHasDescription(bad("Property"), badDesc("Property"));
		database.assertRoomHasDescription(bad("Room"), badDesc("Room"));
		database.assertItemHasDescription(bad("Item"), badDesc("Item"));
		database.assertHasList(bad("List"));
	}

	private static @NonNull String bad(final String name) {
		return "<" + name + "> " + BAD_XML;
	}

	private static @NonNull String badDesc(final String name) {
		return "<" + name + "> Description " + BAD_XML;
	}
}
