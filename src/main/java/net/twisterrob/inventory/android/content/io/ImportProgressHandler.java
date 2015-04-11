package net.twisterrob.inventory.android.content.io;

import java.io.IOException;

import android.support.annotation.StringRes;

import net.twisterrob.inventory.android.content.contract.ParentColumns.Type;

public interface ImportProgressHandler {
	void publishStart(long size);
	void publishIncrement();
	void warning(@StringRes int stringID, Object... args);
	void error(String message);
	void importImage(Type type, long id, String coalesce, String image) throws IOException;
}
