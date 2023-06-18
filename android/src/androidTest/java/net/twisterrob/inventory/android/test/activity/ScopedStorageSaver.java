package net.twisterrob.inventory.android.test.activity;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiObjectNotFoundException;

import net.twisterrob.android.test.automators.DocumentsUiAutomator;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.content.CreateOpenableDocument;

import static net.twisterrob.android.test.automators.UiAutomatorExtensions.clickOn;
import static net.twisterrob.android.test.automators.UiAutomatorExtensions.exists;
import static net.twisterrob.android.test.automators.UiAutomatorExtensions.shortClickOnDescriptionLabel;

/**
 * Equivalent to
 * <pre><code>
 * File source = new File("...");
 * File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 * File target = new File(downloads, name);
 * IOTools.copy(source, target);
 * </code></pre>
 * but in a really roundabout way,
 * because Android 10+ doesn't allow access to {@code Environment.getExternalStoragePublicDirectory}.
 * Workaround is to use the Documents UI to save the file to Downloads using UIAutomator.
 */
public class ScopedStorageSaver extends ComponentActivity {

	private static @NonNull Intent createIntent(@NonNull Context context, @NonNull Uri uri,
			@NonNull String mime, @NonNull String name) {
		return new Intent(context, ScopedStorageSaver.class)
				.setDataAndType(uri, mime)
				.putExtra(Intent.EXTRA_TITLE, name);
	}

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForActivityResult(
				new CreateOpenableDocument(getIntent().getType()),
				new ActivityResultCallback<Uri>() {
					@Override public void onActivityResult(@Nullable Uri result) {
						ScopedStorageSaver.lastFileName = copyToSelectedTarget(result);
						finish();
					}
				}
		).launch(getIntent().getStringExtra(Intent.EXTRA_TITLE));
	}

	private @Nullable String copyToSelectedTarget(@NonNull Uri result) {
		StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskWrites();
		try {
			InputStream source = getContentResolver().openInputStream(getIntent().getData());
			OutputStream target = getContentResolver().openOutputStream(result);
			IOTools.copyStream(source, target);
			DocumentFile document = DocumentFile.fromSingleUri(this, result);
			return document.getName();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to copy to target: " + result, ex);
		} finally {
			StrictMode.setThreadPolicy(originalPolicy);
		}
	}

	/**
	 * Abrupt way to get the result into where it's needed,
	 * because we cannot register {@link #onActivityResult} from a static method.
	 */
	private static String lastFileName;

	public static String createZipInDownloads(@NonNull File file)
			throws PackageManager.NameNotFoundException, UiObjectNotFoundException {
		Context context = InstrumentationRegistry.getInstrumentation().getContext();
		Uri uri = Uri.fromFile(file);
		Intent intent = createIntent(context, uri, "application/zip", file.getName())
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		ScopedStorageSaver.lastFileName = null;
		InstrumentationRegistry.getInstrumentation().getContext().startActivity(intent);
		if (!exists(DocumentsUiAutomator.drawerToolbar())) {
			shortClickOnDescriptionLabel(DocumentsUiAutomator.showRoots());
		}
		DocumentsUiAutomator.selectRootInDrawer("Downloads").clickAndWaitForNewWindow();
		clickOn(DocumentsUiAutomator.save());
		return lastFileName;
	}
}
