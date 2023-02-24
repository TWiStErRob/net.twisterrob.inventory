package net.twisterrob.inventory.android.activity.space;

import java.io.File;

import android.widget.TextView;

import net.twisterrob.android.utils.tools.IOTools;

class GetFolderSizesTask extends GetSizeTask<File> {
	public GetFolderSizesTask(TextView result) {
		super(result);
	}

	@Override protected Long doInBackgroundSafe(File... dirs) {
		long result = 0;
		for (File dir : dirs) {
			publishProgress(result);
			result += IOTools.calculateSize(dir);
		}
		return result;
	}
}
