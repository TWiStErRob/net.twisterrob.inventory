package net.twisterrob.android.utils.tools;

import org.junit.Test;

import static org.junit.Assert.*;

public class AndroidToolsTest {
	@Test public void tryGetAttachedListener() {
		Number n = 5;
		assertNotNull(AndroidTools.tryGetAttachedListener(n, Integer.class));
	}
	@Test public void tryGetAttachedListenerInvalid() {
		Number n = 5;
		assertNull(AndroidTools.tryGetAttachedListener(n, String.class));
	}
}
