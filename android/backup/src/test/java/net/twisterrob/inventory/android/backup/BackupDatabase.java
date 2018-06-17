package net.twisterrob.inventory.android.backup;

import static org.mockito.Mockito.*;

import com.shazam.gwen.collaborators.Asserter;

import net.twisterrob.inventory.android.content.Database;

@SuppressWarnings("UnusedReturnValue")
public class BackupDatabase implements Asserter {
	protected final Database db;
	public BackupDatabase(Database mock) {
		this.db = mock;
	}

	public BackupDatabase transacted() {
		return transacted(true);
	}
	public BackupDatabase transacted(boolean isSuccessful) {
		verify(db).beginTransaction();
		verify(db).endTransaction();
		if (isSuccessful) {
			verify(db).setTransactionSuccessful();
		} else {
			verify(db, never()).setTransactionSuccessful();
		}
		return this;
	}
}
