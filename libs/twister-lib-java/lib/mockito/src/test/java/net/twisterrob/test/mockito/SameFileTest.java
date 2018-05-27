package net.twisterrob.test.mockito;

import java.io.*;
import java.util.function.Consumer;

import org.junit.*;
import org.junit.function.ThrowingRunnable;
import org.mockito.Mock;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static net.twisterrob.test.hamcrest.Matchers.*;

public class SameFileTest {
	private static final String DUMMY_PATH = "a/b";
	/** Different than {@link #DUMMY_PATH}. */
	private static final String DIFFERENT_PATH = "a/c";

	@Rule public MockitoRule mockito = MockitoJUnit.rule();

	/** Irrelevant what this type is, chosen because it accepts an arbitrary argument and is easily mocked. */
	@Mock Consumer<File> mock;

	@Test public void matchesSelfSameObject() {
		File file = new File(DUMMY_PATH);

		mock.accept(file);

		verify(mock).accept(argThat(SameFile.pointsTo(file)));
	}

	@Test public void matchesSelfDifferentObject() {
		mock.accept(new File(DUMMY_PATH));

		verify(mock).accept(argThat(SameFile.pointsTo(new File(DUMMY_PATH))));
	}

	@Test public void matchesAbsoluteExpectation() {
		File file = new File(DUMMY_PATH);

		mock.accept(file);

		verify(mock).accept(argThat(SameFile.pointsTo(file.getAbsoluteFile())));
	}

	@Test public void matchesAbsoluteCall() {
		File file = new File(DUMMY_PATH);

		mock.accept(file.getAbsoluteFile());

		verify(mock).accept(argThat(SameFile.pointsTo(file)));
	}

	@Test public void matchesNull() {
		mock.accept(null);

		verify(mock).accept(argThat(SameFile.pointsTo(null)));
	}

	@Test public void failsDifferentFile() {
		mock.accept(new File(DUMMY_PATH));

		assertThrows(ArgumentsAreDifferent.class, new ThrowingRunnable() {
			@Override public void run() throws ArgumentsAreDifferent {
				verify(mock).accept(argThat(SameFile.pointsTo(new File(DIFFERENT_PATH))));
			}
		});
	}

	@Test public void failsNullCall() {
		mock.accept(null);

		assertThrows(ArgumentsAreDifferent.class, new ThrowingRunnable() {
			@Override public void run() throws ArgumentsAreDifferent {
				verify(mock).accept(argThat(SameFile.pointsTo(new File(DUMMY_PATH))));
			}
		});
	}

	@Test public void failsNullExpectation() {
		mock.accept(new File(DUMMY_PATH));

		assertThrows(ArgumentsAreDifferent.class, new ThrowingRunnable() {
			@Override public void run() throws ArgumentsAreDifferent {
				verify(mock).accept(argThat(SameFile.pointsTo(null)));
			}
		});
	}

	@Test public void failsFileResolution() {
		IOException ex = new IOException("test");
		final File file = mock(File.class, new ThrowsException(ex));
		mock.accept(file);

		Throwable expectedFailure = assertThrows(Throwable.class, new ThrowingRunnable() {
			@Override public void run() {
				verify(mock).accept(argThat(SameFile.pointsTo(file)));
			}
		});

		assertThat(expectedFailure, hasCause(ex));
	}
}
