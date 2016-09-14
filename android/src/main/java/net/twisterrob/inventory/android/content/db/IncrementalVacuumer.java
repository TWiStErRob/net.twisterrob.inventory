package net.twisterrob.inventory.android.content.db;

import java.util.concurrent.Callable;

import org.slf4j.*;

import android.database.sqlite.SQLiteDatabase;

import net.twisterrob.android.utils.tools.DatabaseTools;

import static net.twisterrob.android.utils.tools.DatabaseTools.*;
import static net.twisterrob.java.utils.ObjectTools.*;

public class IncrementalVacuumer implements Callable<Boolean> {
	private static final Logger LOG = LoggerFactory.getLogger(IncrementalVacuumer.class);
	private static final int DEFAULT_MAX_BYTES_TO_FREE = 10 * 1024 * 1024;

	private final SQLiteDatabase db;
	private final long maxBytesToFree;

	public IncrementalVacuumer(SQLiteDatabase database) {
		this(database, DEFAULT_MAX_BYTES_TO_FREE);
	}
	public IncrementalVacuumer(SQLiteDatabase database, long maxBytesToFree) {
		this.db = checkNotNull(database);
		this.maxBytesToFree = maxBytesToFree;
	}

	@Override public Boolean call() throws Exception {
		LOG.trace("Working with {}", DatabaseTools.dbToString(db));
		long vacuumState = DatabaseTools.getPragma(db, Pragma.AUTO_VACUUM);
		if (vacuumState != 2) {
			LOG.trace("auto_vacuum is not INCREMENTAL, ignoring.");
			return false;
		} else {
			LOG.trace("auto_vacuum is INCREMENTAL, checking freelist.");
		}
		long freelistCount = DatabaseTools.getPragma(db, Pragma.FREELIST_COUNT);
		if (freelistCount == 0) {
			LOG.trace("No more pages on freelist, reset auto_vacuum to FULL.");
			DatabaseTools.setPragma(db, Pragma.AUTO_VACUUM, Pragma.AUTO_VACUUM_FULL);
			return false;
		}
		long pageSize = DatabaseTools.getPragma(db, Pragma.PAGE_SIZE);
		if (maxBytesToFree < pageSize) {
			throw new IllegalArgumentException("The maximum bytes to free (" + maxBytesToFree
					+ ") are less than a single page's size (" + pageSize + ") which means no pages would be freed.");
		}
		long pagesToFree = Math.min(maxBytesToFree / pageSize, freelistCount);
		LOG.trace("There are {} pages on freelist occupying {} bytes, freeing {} pages occupying {} bytes.",
				freelistCount, freelistCount * pageSize, pagesToFree, pagesToFree * pageSize);
		int count = DatabaseTools.callPragma(db, Pragma.INCREMENTAL_VACUUM, pagesToFree);
		LOG.trace("Freed {} out of {} requested pages from {} pages on freelist.",
				count, pagesToFree, freelistCount);
		return true;
	}
}
