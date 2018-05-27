package net.twisterrob.test.mockito;

import java.io.File;

import javax.annotation.*;

import org.mockito.ArgumentMatcher;

public class ArgumentMatchers {

	public static @Nonnull ArgumentMatcher<File> pointsTo(final @Nullable File file) {
		return SameFile.pointsTo(file);
	}
}
