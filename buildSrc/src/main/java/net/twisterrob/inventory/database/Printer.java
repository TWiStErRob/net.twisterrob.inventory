package net.twisterrob.inventory.database;

import java.io.*;

public interface Printer {
	void start(Writer output) throws IOException;
	void print(Category c, Writer output) throws IOException;
	void finish(Writer output) throws IOException;
}
