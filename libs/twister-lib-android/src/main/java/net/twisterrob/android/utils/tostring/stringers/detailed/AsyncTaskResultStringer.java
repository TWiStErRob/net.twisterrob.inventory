package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import net.twisterrob.android.utils.concurrent.AsyncTaskResult;
import net.twisterrob.java.utils.tostring.*;

@SuppressWarnings("rawtypes")
public class AsyncTaskResultStringer extends Stringer<AsyncTaskResult> {
	@Override public void toString(@Nonnull ToStringAppender append, AsyncTaskResult result) {
		append.complexProperty("result", result.getResult());
		append.complexProperty("error", result.getError());
		Object[] params = result.getParams();
		append.beginSizedList("params", params.length, true);
		for (int i = 0; i < params.length; i++) {
			append.item(i, params[i]);
		}
		append.endSizedList();
	}
}
