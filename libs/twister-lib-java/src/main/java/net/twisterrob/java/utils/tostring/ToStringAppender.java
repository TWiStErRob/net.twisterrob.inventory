package net.twisterrob.java.utils.tostring;

import javax.annotation.*;

/**
 * The generic methods use recursive toString processing, while the Object ones use straight sb.append.
 */
public interface ToStringAppender {
	void beginSizedList(Object container, int count);
	void beginSizedList(Object container, int count, boolean allowShortcut);
	void beginSizedList(String containerName, int count);
	void beginSizedList(String containerName, int count, boolean allowShortcut);
	void endSizedList();

	void beginPropertyGroup(@Nullable String name);
	void endPropertyGroup();

	<T> void item(@Nullable T value);
	<T> void item(@Nullable T value, Stringer<? super T> toString);
	<T> void item(int index, @Nullable T value);
	<T> void item(@Nonnull String name, @Nullable T value);

	void identity(@Nullable Object id, @Nullable Object name);

	<T> void selfDescribingProperty(@Nonnull T value);
	void rawProperty(@Nonnull String name, @Nullable Object value);
	void typedProperty(@Nonnull String name, @Nullable String type, @Nullable Object value);
	void measuredProperty(@Nonnull String name, @Nullable String measure, @Nullable Object value);
	void formattedProperty(@Nonnull String name, @Nullable String type, String format, Object... args);
	<T> void complexProperty(@Nonnull String name, @Nullable T value);
	<T> void complexProperty(@Nonnull String name, @Nullable T value, Stringer<? super T> toString);
	void booleanProperty(boolean whether, @Nonnull String positive);
	void booleanProperty(boolean whether, @Nonnull String positive, @Nonnull String negative);
}
