package net.twisterrob.inventory.android.backup;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.google.common.base.Preconditions;
import com.shazam.gwen.collaborators.*;

import net.twisterrob.inventory.android.backup.BackupZip.Item;
import net.twisterrob.inventory.android.backup.Importer.ImportProgressHandler;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.inventory.android.content.contract.Type;

class BackupImporter implements Actor, Asserter {
	private static final Logger LOG = LoggerFactory.getLogger(BackupImporter.class);

	private Database dbMock;
	private ProgressDispatcher dispatcherMock;
	private XMLImporter xmlImporterMock;
	private final Function<InputStream, Progress> importer;
	public BackupImporter(Database dbMock, ProgressDispatcher dispatcherMock, XMLImporter xmlImporterMock,
			Function<InputStream, Progress> importer) {
		this.dbMock = Preconditions.checkNotNull(dbMock);
		this.dispatcherMock = Preconditions.checkNotNull(dispatcherMock);
		this.xmlImporterMock = Preconditions.checkNotNull(xmlImporterMock);
		this.importer = Preconditions.checkNotNull(importer);
	}
	public BackupImportResult imports(final BackupZip input) throws Throwable {
		return imports(input, new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				ImportProgressHandler handler = invocation.getArgumentAt(1, ImportProgressHandler.class);
				List<Item> items = input.getItems();
				handler.publishStart(items.size());
				for (Item item : items) {
					if (item.image != null) {
						handler.importImage(Type.Item, item.id, item.name, item.image);
					}
					handler.publishIncrement();
				}
				return null;
			}
		});
	}
	public BackupImportResult imports(final BackupZip input, Answer<Void> importAnswer) throws Throwable {
//		when(dbMock.addImage(any(byte[].class), anyLong())).thenAnswer(new Answer<Long>() {
//			private long id = 1000;
//			@Override public Long answer(InvocationOnMock invocation) throws Throwable {
//				return id++;
//			}
//		});
		if (input.hasXML()) {
			doAnswer(importAnswer)
					.when(xmlImporterMock).doImport(any(InputStream.class), any(ImportProgressHandler.class));
		}
		Progress progress = importer.apply(input.getStream());
		if (progress.failure instanceof AssertionError) {
			throw progress.failure;
		}
		LOG.trace("Import resulted in {}", progress, progress.failure);
		return new BackupImportResult(progress, dispatcherMock);
	}
	public BackupImporter importedXML() throws Throwable {
		verify(xmlImporterMock).doImport(any(InputStream.class), any(ImportProgressHandler.class));
		return this;
	}
}
