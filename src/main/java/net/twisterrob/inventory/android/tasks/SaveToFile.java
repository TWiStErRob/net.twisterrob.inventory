package net.twisterrob.inventory.android.tasks;

import java.io.*;

import org.slf4j.*;

import android.content.Context;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.concurrent.SimpleSafeAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;

public class SaveToFile extends SimpleSafeAsyncTask<File, Void, File> {
	private static final Logger LOG = LoggerFactory.getLogger(SaveToFile.class);

	private final Context context;

	public SaveToFile(Context context) {
		this.context = context;
	}

	@Override
	public File doInBackground(File source) throws IOException {
		return uploadFile(source);
	}

	@Override protected void onResult(File result, File param) {
		LOG.trace("Saved {} as {}", param, result);
		App.toast("Saved " + result);
	}

	@Override protected void onError(@NonNull Exception ex, File param) {
		LOG.error("Cannot upload file: {}", param, ex);
	}

	private File uploadFile(File source) throws IOException {
		File folder = Paths.getImageDirectory(context);
		LOG.info("Saving {} to Folder: {}", source, folder);
		File target = new File(folder, source.getName());
		IOTools.copyFile(source, target);
		return target;
	}
}