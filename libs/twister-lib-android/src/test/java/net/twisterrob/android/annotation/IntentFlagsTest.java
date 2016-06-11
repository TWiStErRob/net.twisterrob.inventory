package net.twisterrob.android.annotation;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static android.content.Intent.*;

public class IntentFlagsTest {
	@Test public void test() {
		String string = IntentFlags.Converter.toString(FLAG_GRANT_READ_URI_PERMISSION | FLAG_FROM_BACKGROUND, null);
		assertThat(string, containsString("[FLAG_GRANT_READ_URI_PERMISSION* | FLAG_FROM_BACKGROUND]"));
	}
}
