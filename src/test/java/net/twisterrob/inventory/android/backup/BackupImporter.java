package net.twisterrob.inventory.android.backup;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.google.common.base.Preconditions;
import com.shazam.gwen.collaborators.*;

import net.twisterrob.inventory.android.backup.BackupZip.Item;
import net.twisterrob.inventory.android.backup.Importer.*;
import net.twisterrob.inventory.android.backup.xml.XMLImporter;
import net.twisterrob.inventory.android.content.contract.Type;

class BackupImporter implements Actor, Asserter {
	private static final Logger LOG = LoggerFactory.getLogger(BackupImporter.class);

	private ImportProgressHandler dispatcherMock;
	private XMLImporter xmlImporterMock;
	private final Consumer<InputStream> importer;
	public BackupImporter(ImportProgressHandler dispatcherMock, XMLImporter xmlImporterMock,
			Consumer<InputStream> importer) {
		this.dispatcherMock = Preconditions.checkNotNull(dispatcherMock);
		this.xmlImporterMock = Preconditions.checkNotNull(xmlImporterMock);
		this.importer = Preconditions.checkNotNull(importer);
	}
	public BackupImportResult imports(final BackupZip input) throws Throwable {
		return imports(input, new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				ImportProgress handler = invocation.getArgumentAt(1, ImportProgress.class);
				ImportImageGetter getter = invocation.getArgumentAt(2, ImportImageGetter.class);
				List<Item> items = input.getItems();
				handler.publishStart(items.size());
				for (Item item : items) {
					if (item.image != null) {
						getter.importImage(Type.Item, item.id, item.name, item.image);
					}
					handler.publishIncrement();
				}
				return null;
			}
		});
	}
	public BackupImportResult imports(final BackupZip input, Answer<Void> importAnswer) throws Throwable {
		if (input.hasXML()) {
			doAnswer(importAnswer)
					.when(xmlImporterMock)
					.doImport(any(InputStream.class), any(ImportProgress.class), any(ImportImageGetter.class));
		}
		importer.accept(input.getStream());
		Progress progress = dispatcherMock.finalProgress();
		LOG.trace("Import resulted in {}", progress, progress.failure);
		return new BackupImportResult(progress, dispatcherMock);
	}
	public BackupImporter importedXML() throws Throwable {
		verify(xmlImporterMock)
				.doImport(any(InputStream.class), any(ImportProgress.class), any(ImportImageGetter.class));
		return this;
	}
}
