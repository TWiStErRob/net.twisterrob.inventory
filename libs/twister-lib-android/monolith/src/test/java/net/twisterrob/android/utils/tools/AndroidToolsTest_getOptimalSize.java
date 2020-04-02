package net.twisterrob.android.utils.tools;

import java.util.*;

import org.junit.Test;

import static org.junit.Assert.*;

import static net.twisterrob.android.utils.tools.CameraSizeEqualTo.*;
import static net.twisterrob.android.utils.tools.CameraSizeHelper.*;

@SuppressWarnings("deprecation")
public class AndroidToolsTest_getOptimalSize {
	// Nexus 7 (has on-screen buttons that are 75-80 px tall)
	// orient=90, rotate=270, landscape=false, size: 1205x800 (1.50625), surface: 800x1205 (0.66390043), preview: 720x480 (1.5), picture: 1024x768 (1.3333334)
	// orient=0, rotate=0, landscape=true, size: 1280x736 (1.7391304), surface: 1280x736 (1.7391304), preview: 1280x720 (1.7777778), picture: 1280x720 (1.7777778)
	private static final List<android.hardware.Camera.Size> NEXUS_7_PREVIEWS = Arrays.asList(
			s(176, 144), s(320, 240), s(352, 288), s(480, 480), s(640, 480), s(704, 576), s(720, 408), s(720, 480),
			s(720, 576), s(768, 432), s(800, 448), s(960, 720), s(1280, 720)
	);
	private static final List<android.hardware.Camera.Size> NEXUS_7_PICTURES = Arrays.asList(
			s(320, 240), s(480, 480), s(640, 480), s(800, 600), s(1024, 768), s(1280, 720), s(1280, 960)
	);
	// Samsung Galaxy S5
	// orient=90, rotate=90, landscape=false, size: 1920x1080 (1.7777778), surface: 1080x1920 (0.5625), preview: 1920x1080 (1.7777778), picture: 1920x1080 (1.7777778)
	// orient=0, rotate=0, landscape=true, size: 1920x1080 (1.7777778), surface: 1920x1080 (1.7777778), preview: 1920x1080 (1.7777778), picture: 1920x1080 (1.7777778)
	private static final List<android.hardware.Camera.Size> S5_PREVIEWS = Arrays.asList(
			s(1920, 1080), s(1440, 1080), s(1280, 720), s(1056, 864), s(960, 720), s(800, 480), s(720, 480),
			s(640, 480), s(352, 288), s(320, 240), s(176, 144)
	);
	private static final List<android.hardware.Camera.Size> S5_PICTURES = Arrays.asList(
			s(5312, 2988), s(3984, 2988), s(3264, 2448), s(3264, 1836), s(2560, 1920), s(2048, 1152), s(1920, 1080),
			s(1280, 960), s(1280, 720), s(800, 480), s(640, 480)
	);

	@Test public void testS5PortraitPreview() {
		assertThat(getOptimalSize(S5_PREVIEWS, 1080, 1920), equalTo(s(1920, 1080)));
	}
	@Test public void testS5PortraitPicture() {
		assertThat(getOptimalSize(S5_PICTURES, 1080, 1920), equalTo(s(5312, 2988)));
	}
	@Test public void testS5LandScapePreview() {
		assertThat(getOptimalSize(S5_PREVIEWS, 1920, 1080), equalTo(s(1920, 1080)));
	}
	@Test public void testS5LandscapePicture() {
		assertThat(getOptimalSize(S5_PICTURES, 1920, 1080), equalTo(s(5312, 2988)));
	}

	@Test public void testNexus7LandscapePreview() {
		assertThat(getOptimalSize(NEXUS_7_PREVIEWS, 1280, 736), equalTo(s(720, 408)));
	}
	@Test public void testNexus7LandscapePicture() {
		assertThat(getOptimalSize(NEXUS_7_PICTURES, 1280, 736), equalTo(s(1280, 720)));
	}
	@Test public void testNexus7PortraitPreview() {
		assertThat(getOptimalSize(NEXUS_7_PREVIEWS, 800, 1205), equalTo(s(720, 480)));
	}
	@Test public void testNexus7PortraitPicture() {
		assertThat(getOptimalSize(NEXUS_7_PICTURES, 800, 1205), equalTo(s(1280, 960)));
	}

	private android.hardware.Camera.Size getOptimalSize(List<android.hardware.Camera.Size> sizes, int w, int h) {
		sortedPrint(sizes, w, h);
		return AndroidTools.getOptimalSize(sizes, w, h);
	}

	private static void sortedPrint(List<android.hardware.Camera.Size> previews, int w, int h) {
		System.out.printf(Locale.ROOT, "Screen size %dx%d (%.3f)%n", w, h, w / (float)h);
		List<android.hardware.Camera.Size> sizes = new ArrayList<>(previews);
		Collections.sort(sizes, new CameraSizeComparator(w, h));
		printSizes(sizes);
	}
}
