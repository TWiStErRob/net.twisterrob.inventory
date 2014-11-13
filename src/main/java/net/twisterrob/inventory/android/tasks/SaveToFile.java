package net.twisterrob.inventory.android.tasks;

import java.io.*;

import org.slf4j.*;

import android.content.Context;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.android.utils.tools.IOTools;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;

public class SaveToFile extends SimpleAsyncTask<File, Void, File> {
	private static final Logger LOG = LoggerFactory.getLogger(SaveToFile.class);

	private final Context context;

	public SaveToFile(Context context) {
		this.context = context;
	}

	@Override
	public File doInBackground(File source) {
		try {
			return uploadFile(source);
		} catch (IOException ex) {
			LOG.error("Cannot upload file: {}", source, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(File result) {
		if (result != null) {
			App.toast("Saved: " + result);
		}
	}

	private File uploadFile(File source) throws IOException {
		File folder = Paths.getImageDirectory(context);
		LOG.info("Saving {} to Folder: {}", source, folder);
		File target = new File(folder, source.getName());
		IOTools.copyFile(source, target);
		return target;
	}
}