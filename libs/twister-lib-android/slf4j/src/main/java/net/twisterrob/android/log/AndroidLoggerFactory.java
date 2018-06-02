package net.twisterrob.android.log;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.ILoggerFactory;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.annotation.*;
import android.util.Log;

/**
 * An implementation of {@link ILoggerFactory} for Android, creating {@link AndroidLogger} instances.
 *
 * @author papp.robert.s@gmail.com
 */
public class AndroidLoggerFactory implements ILoggerFactory {

	private static final String TAG = AndroidLoggerFactory.class.getSimpleName();

	/**
	 * Tag names cannot be longer than {@value} on Android platform.
	 *
	 * @see android/system/core/include/cutils/property.h
	 * @see android/frameworks/base/core/jni/android_util_Log.cpp
	 */
	private static final int TAG_MAX_LENGTH = 23;

	/**
	 * Allow for replacing long package names with aliases.
	 * For example <code>AndroidLoggerFactory.addReplacement("net.twisterrob.app.android", "app")</code>.
	 *
	 * @see AndroidLoggerFactory#addReplacement
	 */
	private static final Map<String, String> REPLACEMENTS = new LinkedHashMap<>();

	static {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
		try {
			Properties properties = new Properties();
			InputStream config = AndroidLoggerFactory.class.getResourceAsStream("/android-logger.properties");
			if (config != null) {
				properties.load(new InputStreamReader(config, "utf-8"));
				for (Object rawKey : properties.keySet()) {
					String searchKey = rawKey.toString();
					if (searchKey.startsWith("replacement.") && !searchKey.endsWith(".with")) {
						String replaceKey = searchKey + ".with";
						String search = properties.getProperty(searchKey);
						String replace = properties.getProperty(replaceKey);
						if (replace == null) {
							replace = "";
						}
						addReplacement(search, replace);
					}
				}
			} else {
				Log.w(TAG, "No android-logger.properties present in APK root, not using any substitutions.\nLogs coming from this logger will have tags like a*.b*.c**EndOfClassName.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	private final ConcurrentMap<String, AndroidLogger> loggerMap = new ConcurrentHashMap<>();

	public AndroidLogger getLogger(@Nullable String name) {
		if (name == null) {
			name = "NULL";
		}
		String replacedName = doReplacements(name);
		AndroidLogger logger = loggerMap.get(replacedName);
		if (logger != null) {
			return logger;
		} else {
			AndroidLogger newLogger = createAndroidLogger(name, replacedName);
			AndroidLogger existingLogger = loggerMap.putIfAbsent(replacedName, newLogger);
			return existingLogger == null? newLogger : existingLogger;
		}
	}

	private AndroidLogger createAndroidLogger(@NonNull String originalName, @NonNull String replacedName) {
		String tag = forceValidName(replacedName); // fix for bug #173
		if (!tag.equals(replacedName)) {
			String message = String.format(Locale.ROOT,
					"Logger name '%1$s' mapped to '%2$s' is too long (>%4$d), using: %3$s.",
					originalName, replacedName, tag, TAG_MAX_LENGTH);
			Log.i(TAG, message);
		}

		return new AndroidLogger(originalName, replacedName, tag);
	}

	/**
	 * Allow for replacing long package names with aliases. Search and replace uses regular expressions.
	 * Replacements may collapse different classes to use the same Android TAG in the end.
	 *
	 * @param regex package name to replace
	 * @param replacement replacement alias
	 *
	 * @see String#replaceFirst(String, String)
	 */
	public static void addReplacement(@NonNull String regex, @NonNull String replacement) {
		REPLACEMENTS.put(regex, replacement);
	}

	private static @NonNull String doReplacements(@NonNull String name) {
		for (Map.Entry<String, String> replacement : REPLACEMENTS.entrySet()) {
			name = name.replaceFirst(replacement.getKey(), replacement.getValue());
		}
		return name;
	}

	/**
	 * Shorten name to sensible tag in case it exceeds maximum length of {@value #TAG_MAX_LENGTH} characters.
	 */
	private static @NonNull String forceValidName(@NonNull String name) {
		if (name.length() > TAG_MAX_LENGTH && name.contains(".")) {
			StringTokenizer st = new StringTokenizer(name, ".");
			if (st.hasMoreTokens()) { // note that empty tokens are skipped, i.e., "aa..bb" has tokens "aa", "bb"
				StringBuilder sb = new StringBuilder();
				do {
					String token = st.nextToken();
					if (token.length() <= 2) { // token of one character appended as is
						sb.append(token);
						sb.append('.');
					} else if (st.hasMoreTokens()) { // truncate all but the last token
						sb.append(token.charAt(0));
						sb.append("*.");
					} else { // last token (usually class name) appended as is
						sb.append(token);
					}
				} while (st.hasMoreTokens());

				name = sb.toString();
			}
		}
		if (name.length() > TAG_MAX_LENGTH && name.contains("$")) {
			int dollar = name.lastIndexOf("$");
			String before = name.substring(0, dollar);
			String after = name.substring(dollar, name.length()); // keep dollar with after
			name = before.replaceAll("\\p{Lower}", "") + after;
		}
		// Either we had no useful dot location at all or name still too long.
		// Take as much as possible from front and back and put a * in the middle.
		if (name.length() > TAG_MAX_LENGTH) {
			int half = TAG_MAX_LENGTH / 2;
			name = name.substring(0, half) + '*' + name.substring(name.length() - half, name.length());
		}
		return name;
	}
}
