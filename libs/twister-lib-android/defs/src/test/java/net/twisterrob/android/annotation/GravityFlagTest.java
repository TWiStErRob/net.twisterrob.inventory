package net.twisterrob.android.annotation;

import java.util.*;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import android.annotation.*;
import android.os.Build.VERSION_CODES;
import android.support.v4.view.GravityCompat;
import android.view.Gravity;

@RunWith(Parameterized.class)
@TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("RtlHardcoded")
public class GravityFlagTest {
	private static final String FLAG_LIST_SEPARATOR = Pattern.quote(GravityFlag.Converter.FLAG_LIST_SEPARATOR);
	private static final String FLAG_LIST_START = GravityFlag.Converter.FLAG_LIST_START;
	private static final String FLAG_LIST_END = GravityFlag.Converter.FLAG_LIST_END;

	@Parameters(name = "gravityToString#{index}({0,number,#})={1}")
	public static Object[][] data() {
		final String RELATIVE = "RELATIVE_LAYOUT_DIRECTION";
		return new Object[][] {
				// single values
				{Gravity.NO_GRAVITY, expect("NO_GRAVITY")},
				{Gravity.CLIP_VERTICAL, expect("CLIP_VERTICAL")},
				{Gravity.CLIP_HORIZONTAL, expect("CLIP_HORIZONTAL")},
				{Gravity.DISPLAY_CLIP_VERTICAL, expect("DISPLAY_CLIP_VERTICAL")},
				{Gravity.DISPLAY_CLIP_HORIZONTAL, expect("DISPLAY_CLIP_HORIZONTAL")},

				{Gravity.TOP, expect("TOP")},
				{Gravity.BOTTOM, expect("BOTTOM")},
				{Gravity.LEFT, expect("LEFT")},
				{Gravity.RIGHT, expect("RIGHT")},
				{Gravity.START, expect("START")},
				{GravityCompat.START, expect("START")},
				{Gravity.END, expect("END")},
				{GravityCompat.END, expect("END")},

				{Gravity.CENTER, expect("CENTER")},
				{Gravity.CENTER_VERTICAL, expect("CENTER_VERTICAL")},
				{Gravity.CENTER_HORIZONTAL, expect("CENTER_HORIZONTAL")},
				{Gravity.FILL, expect("FILL")},
				{Gravity.FILL_VERTICAL, expect("FILL_VERTICAL")},
				{Gravity.FILL_HORIZONTAL, expect("FILL_HORIZONTAL")},

				// logically valid combined values
				{Gravity.TOP | Gravity.LEFT, expect("TOP", "LEFT")},
				{Gravity.TOP | Gravity.RIGHT, expect("TOP", "RIGHT")},
				{Gravity.TOP | Gravity.START, expect("TOP", "START")},
				{Gravity.TOP | Gravity.END, expect("TOP", "END")},
				{Gravity.TOP | Gravity.CENTER_HORIZONTAL, expect("TOP", "CENTER_HORIZONTAL")},
				{Gravity.TOP | Gravity.FILL_HORIZONTAL, expect("TOP", "FILL_HORIZONTAL")},

				{Gravity.BOTTOM | Gravity.LEFT, expect("BOTTOM", "LEFT")},
				{Gravity.BOTTOM | Gravity.RIGHT, expect("BOTTOM", "RIGHT")},
				{Gravity.BOTTOM | Gravity.START, expect("BOTTOM", "START")},
				{Gravity.BOTTOM | Gravity.END, expect("BOTTOM", "END")},
				{Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, expect("BOTTOM", "CENTER_HORIZONTAL")},
				{Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, expect("BOTTOM", "FILL_HORIZONTAL")},

				{Gravity.CENTER_VERTICAL | Gravity.LEFT, expect("CENTER_VERTICAL", "LEFT")},
				{Gravity.CENTER_VERTICAL | Gravity.RIGHT, expect("CENTER_VERTICAL", "RIGHT")},
				{Gravity.CENTER_VERTICAL | Gravity.START, expect("CENTER_VERTICAL", "START")},
				{Gravity.CENTER_VERTICAL | Gravity.END, expect("CENTER_VERTICAL", "END")},

				{Gravity.FILL_VERTICAL | Gravity.LEFT, expect("FILL_VERTICAL", "LEFT")},
				{Gravity.FILL_VERTICAL | Gravity.RIGHT, expect("FILL_VERTICAL", "RIGHT")},
				{Gravity.FILL_VERTICAL | Gravity.START, expect("FILL_VERTICAL", "START")},
				{Gravity.FILL_VERTICAL | Gravity.END, expect("FILL_VERTICAL", "END")},
				{Gravity.FILL_VERTICAL | Gravity.CENTER_HORIZONTAL, expect("FILL_VERTICAL", "CENTER_HORIZONTAL")},

				// redundant specifiers
				{Gravity.TOP | Gravity.BOTTOM | Gravity.LEFT | Gravity.RIGHT, expect("FILL")},
				{Gravity.TOP | Gravity.BOTTOM | Gravity.START | Gravity.END, expect("FILL", RELATIVE)},
				{Gravity.TOP | Gravity.BOTTOM | Gravity.LEFT | Gravity.START, expect("FILL_VERTICAL", "START")},
				{Gravity.START | Gravity.END | Gravity.LEFT | Gravity.RIGHT, expect("FILL_HORIZONTAL", RELATIVE)},
				{Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, expect("CENTER")},
				{Gravity.CENTER_VERTICAL | Gravity.CENTER, expect("CENTER")},
				{Gravity.CENTER_HORIZONTAL | Gravity.CENTER, expect("CENTER")},

				// redundant or invalid with FILL
				{Gravity.TOP | Gravity.FILL, expect("FILL")},
				{Gravity.BOTTOM | Gravity.FILL, expect("FILL")},
				{Gravity.LEFT | Gravity.FILL, expect("FILL")},
				{Gravity.RIGHT | Gravity.FILL, expect("FILL")},
				{Gravity.START | Gravity.FILL, expect("FILL", RELATIVE)},
				{GravityCompat.START | Gravity.FILL, expect("FILL", RELATIVE)},
				{Gravity.END | Gravity.FILL, expect("FILL", RELATIVE)},
				{GravityCompat.END | Gravity.FILL, expect("FILL", RELATIVE)},

				{Gravity.CENTER | Gravity.FILL, expect("FILL")},
				{Gravity.CENTER_VERTICAL | Gravity.FILL, expect("FILL")},
				{Gravity.CENTER_HORIZONTAL | Gravity.FILL, expect("FILL")},
				{Gravity.FILL_VERTICAL | Gravity.FILL, expect("FILL")},
				{Gravity.FILL_HORIZONTAL | Gravity.FILL, expect("FILL")},

				// logically invalid combined values
				{Gravity.CENTER | Gravity.RELATIVE_LAYOUT_DIRECTION, expect("CENTER", RELATIVE)},
				{Gravity.START | Gravity.END, expect("FILL_HORIZONTAL", RELATIVE)},
				{Gravity.TOP | Gravity.CENTER, expect("TOP", "CENTER_HORIZONTAL")},
				{Gravity.BOTTOM | Gravity.CENTER, expect("BOTTOM", "CENTER_HORIZONTAL")},
				{Gravity.LEFT | Gravity.CENTER, expect("CENTER_VERTICAL", "LEFT")},
				{Gravity.RIGHT | Gravity.CENTER, expect("CENTER_VERTICAL", "RIGHT")},
				{Gravity.START | Gravity.CENTER, expect("CENTER_VERTICAL", "START")},
				{Gravity.END | Gravity.CENTER, expect("CENTER_VERTICAL", "END")},
				{Gravity.FILL_VERTICAL | Gravity.CENTER, expect("FILL_VERTICAL", "CENTER_HORIZONTAL")},
				{Gravity.FILL_HORIZONTAL | Gravity.CENTER, expect("CENTER_VERTICAL", "FILL_HORIZONTAL")},

				// special values
				{Gravity.FILL_VERTICAL | Gravity.LEFT | Gravity.CLIP_VERTICAL | Gravity.DISPLAY_CLIP_HORIZONTAL,
						expect("FILL_VERTICAL", "CLIP_VERTICAL", "LEFT", "DISPLAY_CLIP_HORIZONTAL")},
				{Gravity.CENTER | Gravity.CLIP_HORIZONTAL | Gravity.DISPLAY_CLIP_HORIZONTAL,
						expect("CENTER", "CLIP_HORIZONTAL", "DISPLAY_CLIP_HORIZONTAL")},
				{Gravity.START | Gravity.END | Gravity.TOP | Gravity.BOTTOM
						| Gravity.CLIP_VERTICAL | Gravity.CLIP_HORIZONTAL
						| Gravity.DISPLAY_CLIP_VERTICAL | Gravity.DISPLAY_CLIP_HORIZONTAL,
						expect("FILL", RELATIVE,
								"CLIP_VERTICAL", "CLIP_HORIZONTAL",
								"DISPLAY_CLIP_VERTICAL", "DISPLAY_CLIP_HORIZONTAL")}
		};
	}

	private final int input;
	private final Expectation expected;

	public GravityFlagTest(int input, Expectation expected) {
		this.input = input;
		this.expected = expected;
	}

	@Test public void test() {
		String gravity = GravityFlag.Converter.toString(input);
		assertThat(gravity, allOf(containsString(FLAG_LIST_START), containsString(FLAG_LIST_END)));
		String flagList = gravity.substring(gravity.indexOf(FLAG_LIST_START) + 1, gravity.indexOf(FLAG_LIST_END));
		String[] flags = flagList.split(FLAG_LIST_SEPARATOR);
		assertThat(String.format(Locale.ROOT, "Returned gravity: \"%s\"", gravity),
				Arrays.asList(flags), containsInAnyOrder(expected.values()));
	}

	private static Expectation expect(String... expecteds) {
		return new Expectation(expecteds);
	}

	private static class Expectation {
		private final String[] expecteds;
		private Expectation(String... expecteds) {
			this.expecteds = expecteds;
		}
		public String[] values() {
			return expecteds;
		}
		@Override public String toString() {
			return Arrays.toString(expecteds);
		}
	}
}
