package net.twisterrob.android.utils.concurrent;

public interface Executable<Param> {
	@SuppressWarnings("unchecked")
	void executeParallel(Param... params);
	@SuppressWarnings("unchecked")
	void executeSerial(Param... params);
}
