package net.twisterrob.android.utils.concurrent;

public class AsyncTaskResult<Param, Result> {
	private final Param[] params;
	private final Result result;
	private final Exception error;

	@SafeVarargs public AsyncTaskResult(Result result, Param... params) {
		this.params = params;
		this.result = result;
		this.error = null;
	}

	@SafeVarargs public AsyncTaskResult(Exception error, Param... params) {
		this.params = params;
		this.result = null;
		this.error = error;
	}
	public Param[] getParams() {
		return params;
	}
	public Result getResult() {
		return result;
	}
	public Exception getError() {
		return error;
	}
}