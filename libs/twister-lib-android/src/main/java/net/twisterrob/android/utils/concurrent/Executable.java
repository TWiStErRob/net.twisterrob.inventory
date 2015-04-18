package net.twisterrob.android.utils.concurrent;

public interface Executable<Param> {
	void executeParallel(Param... params);
	void executeSerial(Param... params);
}
