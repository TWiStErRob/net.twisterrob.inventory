package net.twisterrob.android.test.junit.internal;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;

import android.app.Instrumentation;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import static android.os.Build.VERSION.*;
import static android.os.Build.VERSION_CODES.*;

import dalvik.system.*;

import net.twisterrob.android.test.junit.AndroidJUnitRunner;
import net.twisterrob.java.utils.ReflectionTools;

/**
 * This is necessary to find extracted secondary dex files by {@link MultiDex}.
 * It is needed to make sure {@link android.support.test.internal.runner.ClassPathScanner}
 * scans all the dex files for tests.
 * By default it only looks for {@code Instrumentation.getContext().getPackageCodePath()},
 * as seen in {@code buildRequest}.
 *
 * @see android.support.test.internal.runner.ClassPathScanner#addEntriesFromPath
 * @see android.support.test.runner.AndroidJUnitRunner#buildRequest
 */
public class DexPathListReflection {

	private final @NonNull Instrumentation instr;
	private final @NonNull Logger LOG;

	public DexPathListReflection(@NonNull Instrumentation instrumentation, @NonNull Logger log) {
		this.instr = instrumentation;
		this.LOG = log;
	}

	/**
	 * This method returns all the archives that could contain test files based on the instrumentation APK.
	 *
	 * For example the {@link MultiDex}'d classpath looks like this (API 19):
	 * <pre>
	 * I/MonitoringInstr: Setting context classloader to
	 * 'dalvik.system.PathClassLoader[DexPathList[
	 * 	[
	 * 		zip file "/system/framework/android.test.runner.jar",
	 * 		zip file "/data/app/net.twisterrob.inventory.data.debug.test-1.apk",
	 * 		zip file "/data/app/net.twisterrob.inventory.data.debug-1.apk",
	 * 		zip file "/data/data/net.twisterrob.inventory.data.debug/code_cache/secondary-dexes/net.twisterrob.inventory.data.debug-1.apk.classes2.zip",
	 * 		zip file "/data/data/net.twisterrob.inventory.data.debug/code_cache/net.twisterrob.inventory.data.debug.test.secondary-dexes/net.twisterrob.inventory.data.debug.test-1.apk.classes2.zip"
	 * 	],
	 * 	nativeLibraryDirectories=[
	 * 		/data/app-lib/net.twisterrob.inventory.data.debug.test-1,
	 * 		/data/app-lib/net.twisterrob.inventory.data.debug-1,
	 * 		/vendor/lib,
	 * 		/system/lib
	 * 	]
	 * ]]'
	 * </pre>
	 *
	 * From the above, the default is:
	 * <pre>/data/app/net.twisterrob.inventory.data.debug.test-1.apk</pre>
	 * but we also need:
	 * <pre>/data/data/net.twisterrob.inventory.data.debug/code_cache/net.twisterrob.inventory.data.debug.test.secondary-dexes/net.twisterrob.inventory.data.debug.test-1.apk.classes2.zip</pre>
	 *
	 * This method returns both.
	 *
	 * @return list of {@link File}s pointing to either an Instrumentation APK or a DEX ZIP file.
	 */
	public @NonNull List<File> dexClasspath() {
		ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites(); // see EOF
		try {
			installMultiDex();
			return scanClassloaderForClasspath();
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	@SuppressWarnings({"ConstantConditions", "deprecation"})
	private @NonNull List<File> scanClassloaderForClasspath() {
		List<File> cp = new ArrayList<>();
		ClassLoader loader = AndroidJUnitRunner.class.getClassLoader();
		if (SDK_INT < ICE_CREAM_SANDWICH) {
			// https://android.googlesource.com/platform/libcore/+/gingerbread-release/dalvik/src/main/java/dalvik/system/PathClassLoader.java#40
			LOG.trace("Expecting val Class.classLoader: {} = {}", PathClassLoader.class, loader);
			DexFile[] mDexs = ReflectionTools.get(loader, "mDexs");
			LOG.trace("Expecting val PathClassLoader.mDexs: DexFile[] = {}", (Object)mDexs);
			for (DexFile dex : mDexs) {
				LOG.trace("Expecting a DexFile inside mDexs: {}", dex);
				File file = new File(dex.getName());
				LOG.trace("Expecting val DexFile.mFileName: String = {}", dex.getName());
				if (file.getAbsolutePath().contains(instr.getContext().getPackageName())) {
					if (file.getName().endsWith(".zip")) {
						generateODEX(file);
					} else {
						// It's APK and it'll work fine because the system already did the dexopt.
						// Otherwise this code wouldn't be running.
					}
					cp.add(file);
				}
			}
		} else if (ICE_CREAM_SANDWICH <= SDK_INT && SDK_INT <= 29) {
			LOG.trace("Expecting val Class.classLoader: {} = {}", BaseDexClassLoader.class, loader);
			Object pathList = ReflectionTools.get(loader, "pathList");
			LOG.trace("Expecting val BaseDexClassLoader.pathList: DexPathList = {}", pathList);
			Object[] dexElements = ReflectionTools.get(pathList, "dexElements");
			LOG.trace("Expecting val DexPathList.dexElements: Element[] = {}", dexElements);
			for (Object dexElement : dexElements) {
				LOG.trace("Expecting a DexPathList.Element for dexElement: {}", dexElement);
				File file = dexElementFile(dexElement);
				LOG.trace("Expecting val DexPathList.Element.*: {} = {}", File.class, file);
				if (file.getAbsolutePath().contains(instr.getContext().getPackageName())) {
					if (file.getName().endsWith(".zip")) {
						generateODEX(file);
					} else {
						// It's APK and it'll work fine because the system already did the dexopt.
						// Otherwise this code wouldn't be running.
					}
					cp.add(file);
				}
			}
		} else if (P < SDK_INT) {
			// Don't know what will happen in the future.
			// DexFile was deprecated in 26 and scheduled to be removed.
			LOG.trace("Skipping val Class.classLoader = {}", loader);
		}
		return cp;
	}

	private void installMultiDex() {
		// MultiDex is normally in android.support.test.runner.MonitoringInstrumentation.onCreate.
		// Need to install it here, because this is used before super.onCreate in AndroidJUnitRunner.
		// Without this, it can't see the extracted DexPathList files.
		MultiDex.installInstrumentation(instr.getContext(), instr.getTargetContext());
	}

	/**
	 * Based on examination of {@code https://android.googlesource.com/platform/libcore/+/<ID>-release/dalvik/src/main/java/dalvik/system/DexPathList.java}
	 * where {@code <ID>} is listed below.
	 * The file structures of {@code static class Element} are as follows:
	 * <ul>
	 * <li>
	 * <b>ics-mr1, jb</b>
	 * <pre><code>
	 * public final File file;
	 * public final ZipFile zipFile;
	 * public final DexFile dexFile;
	 * </code></pre>
	 * </li>
	 * <li>
	 * <b>kitkat, lollipop, lollipop-mr1</b>
	 * <pre><code>
	 * private final File file;
	 * private final boolean isDirectory;
	 * private final File zip;
	 * private final DexFile dexFile;
	 * </code></pre>
	 * </li>
	 * <li>
	 * <b>marshmallow, nougat</b>
	 * <pre><code>
	 * private final File dir;
	 * private final boolean isDirectory;
	 * private final File zip;
	 * private final DexFile dexFile;
	 * </code></pre>
	 * </li>
	 * <li>
	 * <b>oreo, pie, android10</b>
	 * <pre><code>
	 * // A file denoting a zip file (in case of a resource jar or a dex jar), or a directory (only when dexFile is null).
	 * private final File path;
	 * private final DexFile dexFile;
	 * private ClassPathURLStreamHandler urlHandler;
	 * private boolean initialized;
	 * </code></pre>
	 * </li>
	 * </ul>
	 *
	 * @param dexElement dalvik.system.DexPathList.Element
	 * @see dalvik.system.DexPathList.Element
	 */
	@SuppressWarnings("ConstantConditions")
	static @NonNull File dexElementFile(Object dexElement) {
		if (SDK_INT < ICE_CREAM_SANDWICH) {
			throw new IllegalStateException("Shouldn't be called below API 11.");
		} else if (ICE_CREAM_SANDWICH <= SDK_INT && SDK_INT <= LOLLIPOP_MR1) {
			return ReflectionTools.get(dexElement, "file");
		} else if (M <= SDK_INT && SDK_INT <= N_MR1) {
			return ReflectionTools.get(dexElement, "dir");
		} else if (O <= SDK_INT && SDK_INT <= 29) {
			return ReflectionTools.get(dexElement, "path");
		} else { // if (29 < SDK_INT)
			throw new IllegalStateException("Shouldn't be called on Android 10 and above.");
		}
	}

	/**
	 * For example given this entry:
	 * <pre>/data/data/${package}/code_cache/${package}.test.secondary-dexes/${package}.test-2.apk.classes2.zip</pre>
	 *
	 * let's generate:
	 * <pre>/data/data/${package}/code_cache/${package}.test.secondary-dexes/${package}.test-2.apk.classes2.odex</pre>
	 *
	 * This is necessary to prevent
	 * <pre>
	 * E/dalvikvm: Dex cache directory isn't writable: /data/dalvik-cache
	 * E/TestRequestBuilder: Failed to scan classes
	 *     java.io.IOException: unable to open DEX file
	 *         at dalvik.system.DexFile.openDexFileNative(Native Method)
	 * </pre>
	 * by taking the
	 * <a href="https://android.googlesource.com/platform/dalvik/+/android-4.4.4_r2/vm/JarFile.cpp#185">alternative route</a>
	 * without cache when opening the zip file in the future with {@code new DexFile(zip.getAbsolutePath())}.
	 *
	 * @see android.support.test.internal.runner.ClassPathScanner#addEntriesFromPath which uses {@code new DexFile()}.
	 */
	@SuppressWarnings("deprecation") // DexFile and loadDex are both deprecated.
	private void generateODEX(File zip) {
		String zipName = zip.getAbsolutePath();
		String noExtName = zipName.substring(0, zipName.length() - ".zip".length());
		String odexName = noExtName + ".odex";
		try {
			LOG.trace("Generating ODEX {} for {}", odexName, zipName);
			// Sadly this will sometimes crash on API 9.
			// https://issuetracker.google.com/issues/36907533
			// addEntriesFromPath will open and close the same file.
			// The only solution I found to this is replacing what createClassPathScanner creates:
			// android.support.test.internal.runner.TestRequestBuilder.createClassPathScanner
			// android.support.test.runner.AndroidJUnitRunner.createTestRequestBuilder
			// and simply doing the odexing there instead of here,
			// but androidx will likely change that API and dropping API <14 anyway.
			DexFile.loadDex(zipName, odexName, 0).close();
			LOG.trace("Generated ODEX {}", odexName);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Cannot generate ODEX next to ZIP: " + zipName, e);
		}
	}
}

/* StrictMode read/write exemption is needed because MultiDex/DexFile do file operations:
StrictMode policy violation; ~duration=1203 ms: android.os.StrictMode$StrictModeDiskReadViolation: policy=2239 violation=2
        at android.os.StrictMode$AndroidBlockGuardPolicy.onReadFromDisk(StrictMode.java:1135)
        at libcore.io.BlockGuardOs.open(BlockGuardOs.java:106)
        at libcore.io.IoBridge.open(IoBridge.java:393)
        at java.io.RandomAccessFile.<init>(RandomAccessFile.java:118)
        at android.support.multidex.ZipUtil.getZipCrc(ZipUtil.java:55)
        at android.support.multidex.MultiDexExtractor.getZipCrc(MultiDexExtractor.java:234)
        at android.support.multidex.MultiDexExtractor.<init>(MultiDexExtractor.java:98)
        at android.support.multidex.MultiDex.doInstallation(MultiDex.java:257)
        at android.support.multidex.MultiDex.installInstrumentation(MultiDex.java:170)
        at net.twisterrob.android.test.junit.AndroidJUnitRunner.onCreate(AndroidJUnitRunner.java:51)
        at android.app.ActivityThread.handleBindApplication(ActivityThread.java:4335)
        at android.app.ActivityThread.access$1500(ActivityThread.java:135)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1256)
        at android.os.Handler.dispatchMessage(Handler.java:102)
        at android.os.Looper.loop(Looper.java:136)
        at android.app.ActivityThread.main(ActivityThread.java:5017)
        at java.lang.reflect.Method.invokeNative(Native Method)
        at java.lang.reflect.Method.invoke(Method.java:515)
        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:779)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:595)
        at dalvik.system.NativeStart.main(Native Method)
*/
