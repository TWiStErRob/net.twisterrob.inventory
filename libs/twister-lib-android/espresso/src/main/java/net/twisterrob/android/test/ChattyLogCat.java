package net.twisterrob.android.test;

import java.io.*;

import org.slf4j.*;

import android.os.Build.*;

import androidx.annotation.NonNull;

import net.twisterrob.java.io.IOTools;

/**
 * @see <a href="https://stackoverflow.com/a/44975526/253468">Idea</a>
 * @see <a href="https://developer.android.com/studio/command-line/logcat#quotes">-p / -P docs</a>
 */
// FIXME figure out why not working
public class ChattyLogCat {
	private static final Logger LOG = LoggerFactory.getLogger(ChattyLogCat.class);

	private static final String DEFAULT_BLACKLIST = "~! ~1000/!";

	/**
	 * Version where chatty feature in LogCat was introduced.
	 */
	private static final int CHATTY_VERSION = VERSION_CODES.N;
	
	static {
		if (!isChattyAvailable()) {
			LOG.debug("Chatty is not available on this system, all operations will be no-op.");
		}
	}

	private String lastBlackWhiteList = DEFAULT_BLACKLIST;

	public void iAmNotChatty() {
		int pid = android.os.Process.myPid();
		setBlackWhiteList(String.valueOf(pid));
	}

	public synchronized void saveBlackWhiteList() {
		lastBlackWhiteList = getCurrentBlackWhiteList();
	}
	public synchronized void restoreLastBlackWhiteList() {
		setBlackWhiteList(lastBlackWhiteList);
		lastBlackWhiteList = DEFAULT_BLACKLIST;
	}

	private @NonNull String getCurrentBlackWhiteList() {
		if (!isChattyAvailable()) {
			return DEFAULT_BLACKLIST;
		}
		try {
			InputStream stream = Runtime.getRuntime().exec(new String[] {"logcat", "-p"}).getInputStream();
			String currentBlackWhiteList = IOTools.readAll(stream).trim();
			LOG.trace("Received '{}' from `logcat -p`", currentBlackWhiteList);
			return currentBlackWhiteList;
		} catch (IOException ex) {
			LOG.error("Cannot get `logcat -p`", ex);
			return DEFAULT_BLACKLIST;
		}
	}

	private void setBlackWhiteList(@NonNull String blackWhiteList) {
		if (!isChattyAvailable()) {
			return;
		}
		LOG.trace("Setting black/white list: '{}'", blackWhiteList);
		try {
			Runtime.getRuntime().exec(new String[] {"logcat", "-P", "'" + blackWhiteList + "'"}).waitFor();
		} catch (IOException | InterruptedException ex) {
			LOG.error("Cannot set `logcat -P`: {}", blackWhiteList, ex);
		}
	}


	private static boolean isChattyAvailable() {
		return CHATTY_VERSION <= VERSION.SDK_INT;
	}
}
