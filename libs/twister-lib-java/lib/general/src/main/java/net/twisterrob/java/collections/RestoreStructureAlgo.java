package net.twisterrob.java.collections;

import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.annotation.*;

public abstract class RestoreStructureAlgo<Container, Item, Result> {
	public final Result run(@Nonnull Container data) {
		int lastLevel = getLevel(null);
		Iterator<Item> iterator = start(data);
		while (iterator.hasNext()) {
			Item item = iterator.next();
			int level = getLevel(item);

			if (level < lastLevel) {
				for (int i = lastLevel; level < i; --i) {
					onDecrementLevel(i);
				}
			}
			if (lastLevel < level) {
				for (int i = lastLevel + 1; i <= level; ++i) {
					onIncrementLevel(i, i == level? item : null);
				}
			}

			onEntity(level, item);

			lastLevel = level;
		}
		if (0 < lastLevel) {
			for (int i = lastLevel; 0 < i; --i) {
				onDecrementLevel(i);
			}
		}
		return finish();
	}

	protected abstract @Nonnull Iterator<Item> start(@Nonnull Container data);
	/** null will be called and should return with default level */
	protected abstract int getLevel(@Nullable Item item);
	protected abstract void onIncrementLevel(int level, @Nullable Item item);
	protected abstract void onEntity(int level, @Nonnull Item item);
	protected abstract void onDecrementLevel(int level);
	protected abstract Result finish();

	public Callable<Result> toCallable(@Nonnull final Container data) {
		return new Callable<Result>() {
			@Override public Result call() {
				return RestoreStructureAlgo.this.run(data);
			}
		};
	}
}
