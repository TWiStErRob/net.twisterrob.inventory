package net.twisterrob.inventory.android.backup.importers;

import org.junit.*;
import org.mockito.*;
import org.mockito.junit.*;
import org.slf4j.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.github.stefanbirkner.fishbowl.Statement;
import com.shazam.gwen.Gwen;

import static com.github.stefanbirkner.fishbowl.Fishbowl.*;

import net.twisterrob.android.test.LoggingAnswer;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.content.Database;
import net.twisterrob.test.*;

public class BackupTransactingImporterTest {
	private static final Logger LOG = LoggerFactory.getLogger(BackupTransactingImporterTest.class);

	public static final Object INPUT = new Object();
	@Rule public MockitoRule mockito = MockitoJUnit.rule();

	@Mock ImportProgressHandler progress;
	@Mock ZipImporter<Object> inner;
	@Mock Database db;
	@InjectMocks private BackupTransactingImporter<Object> importer;
	@InjectMocks private BackupDatabase database;

	@Before public void setUp() {
		doAnswer(new LoggingAnswer<>(LOG)).when(progress).warning(anyString());
		doAnswer(new LoggingAnswer<>(LOG)).when(progress).error(anyString());
	}

	@Test public void testInputPassed() throws Exception {
		importer.importFrom(INPUT);

		verify(inner).importFrom(same(INPUT));
	}

	@Test public void testSuccess() throws Exception {
		importer.importFrom(INPUT);

		Gwen.then(database).transacted();
	}

	@Test public void testSuccessCannotCommit() {
		Throwable failure = new TestRuntimeException("test cannot commit");
		doThrow(failure).when(db).endTransaction();

		Throwable thrown = exceptionThrownBy(new Statement() {
			@Override public void evaluate() throws Throwable {
				importer.importFrom(INPUT);
			}
		});

		assertSame(failure, thrown);
		Gwen.then(database).transacted();
	}

	@Test public void testSuccessCannotCommitSuppressed() throws Exception {
		Throwable dbFailure = new TestRuntimeException("test cannot commit");
		doThrow(dbFailure).when(db).endTransaction();
		Throwable innerFailure = new TestRuntimeException();
		doThrow(innerFailure).when(inner).importFrom(any());

		Throwable thrown = exceptionThrownBy(new Statement() {
			@Override public void evaluate() throws Throwable {
				importer.importFrom(INPUT);
			}
		});

		assertSame(innerFailure, thrown);
		Gwen.then(database).transacted(false);
		verify(progress).error(contains(dbFailure.getMessage()));
	}

	@Test public void testFailureRuntime() throws Exception {
		Throwable failure = new TestRuntimeException();
		doThrow(failure).when(inner).importFrom(any());

		Throwable thrown = exceptionThrownBy(new Statement() {
			@Override public void evaluate() throws Throwable {
				importer.importFrom(INPUT);
			}
		});

		assertSame(failure, thrown);
		Gwen.then(database).transacted(false);
	}

	@Test public void testFailureChecked() throws Exception {
		Throwable failure = new TestCheckedException();
		doThrow(failure).when(inner).importFrom(any());

		Throwable thrown = exceptionThrownBy(new Statement() {
			@Override public void evaluate() throws Throwable {
				importer.importFrom(INPUT);
			}
		});

		assertSame(failure, thrown);
		Gwen.then(database).transacted(false);
	}

	@Test public void testFailureError() throws Exception {
		Throwable failure = new TestError();
		doThrow(failure).when(inner).importFrom(any());

		Throwable thrown = exceptionThrownBy(new Statement() {
			@Override public void evaluate() throws Throwable {
				importer.importFrom(INPUT);
			}
		});

		assertSame(failure, thrown);
		Gwen.then(database).transacted(false);
	}
}
