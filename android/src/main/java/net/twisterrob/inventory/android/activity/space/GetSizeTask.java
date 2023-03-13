package net.twisterrob.inventory.android.activity.space;

import java.util.Arrays;

import org.slf4j.*;

import android.annotation.SuppressLint;
import android.text.format.Formatter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.concurrent.SafeAsyncTask;
import net.twisterrob.inventory.android.*;

@SuppressLint("StaticFieldLeak") // TODO see ManageSpaceActivity
abstract class GetSizeTask<Param> extends SafeAsyncTask<Param, Long, Long> {
	private static final Logger LOG = LoggerFactory.getLogger(GetSizeTask.class);
	private final TextView result;
	public GetSizeTask(TextView result) {
		this.result = result;
	}
	@Override protected void onPreExecute() {
		result.setText(R.string.manage_space_calculating);
	}

	@Override protected void onProgressUpdate(Long... values) {
		//result.setText("Calculating... (" + getSize(values[0]) + ")");
	}

	@SafeVarargs
	@Override protected final void onResult(Long size, Param... params) {
		result.setText(format(size));
	}

	@SuppressWarnings("varargs")
	@SafeVarargs
	@Override protected final void onError(@NonNull Exception ex, Param... params) {
		LOG.error("Cannot get size of {}", Arrays.toString(params), ex);
		result.setText("?");
		App.toast(App.getError(ex, "Cannot get size of " + Arrays.toString(params)));
	}

	private String format(long size) {
		return Formatter.formatFileSize(result.getContext(), size);
	}
}
