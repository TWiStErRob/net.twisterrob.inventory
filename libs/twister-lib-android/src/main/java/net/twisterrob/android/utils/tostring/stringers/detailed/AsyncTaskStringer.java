package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.util.concurrent.*;

import javax.annotation.Nonnull;

import android.os.AsyncTask;

import net.twisterrob.java.utils.StringTools;
import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings("rawtypes")
public class AsyncTaskStringer extends Stringer<AsyncTask> {
	@Override public void toString(@Nonnull ToStringAppender append, AsyncTask task) {
		append.identity(StringTools.hashString(task), null);
		append.rawProperty("status", task.getStatus());
		append.booleanProperty(task.isCancelled(), "cancelled");
		try {
			Object result = task.get(0, TimeUnit.MILLISECONDS);
			append.complexProperty("result", result);
		} catch (CancellationException e) {
			append.booleanProperty(true, "cancelled");
		} catch (InterruptedException e) {
			append.booleanProperty(true, "interrupted");
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			append.rawProperty("error", e);
		} catch (TimeoutException e) {
			append.booleanProperty(true, "no result yet");
		}
	}
}
