package net.twisterrob.android.utils.concurrent;

public interface Executable<Param> {
	@SuppressWarnings("unchecked")
	public void executeParallel(Param... params);
	@SuppressWarnings("unchecked")
	public void executeSerial(Param... params);
}
