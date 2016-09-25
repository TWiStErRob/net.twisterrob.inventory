package net.twisterrob.inventory.android.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static android.support.test.InstrumentationRegistry.*;

import net.twisterrob.inventory.android.content.Database;

/**
 * It is safe to apply this rule at any time,
 * because it won't open the database until the first query on the returned DB from {@link #get()}.
 */
public class TestDatabaseRule implements TestRule {
	private Database db;

	/** @deprecated FIXME get rid of usages in assertThat in favor of DatabaseActor */
	public Database get() {
		return db;
	}

	@Override public Statement apply(final Statement base, Description description) {
		return new DatabaseStatement(base);
	}

	private class DatabaseStatement extends Statement {
		private final Statement base;
		public DatabaseStatement(Statement base) {
			this.base = base;
		}
		@Override public void evaluate() throws Throwable {
			try {
				db = new Database(getTargetContext(), getContext().getResources());
				base.evaluate();
			} finally {
				db.getWritableDatabase().close();
			}
		}
	}
}
