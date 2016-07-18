package net.twisterrob.java.utils.tostring;

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public class ToStringer implements ToStringAppender {
	private static final Logger LOG = LoggerFactory.getLogger(ToStringer.class);
	private static final boolean LOG_ALL = Boolean.parseBoolean("false");
	private final StringerRepo repo;

	private final StringBuilder sb = new StringBuilder();
	private final Formatter f = new Formatter(sb, Locale.ROOT);
	private final Object value;
	private final Stringer<Object> toString;

	public <T> ToStringer(StringerRepo repo, T value) {
		this(repo, value, repo.findByValue(value));
	}
	public <T> ToStringer(StringerRepo repo, T value, boolean expanded) {
		this(repo, value, expanded, repo.findByValue(value));
	}
	public <T> ToStringer(StringerRepo repo, T value, Stringer<? super T> toString) {
		this(repo, value, true, toString);
	}
	public <T> ToStringer(StringerRepo repo, T value, boolean expanded, Stringer<? super T> toString) {
		this.repo = repo;
		this.value = value;
		@SuppressWarnings("unchecked") Stringer<Object> safeStringer = (Stringer<Object>)toString;
		this.toString = safeStringer;
		this.context.style = expanded? new Style() : new ShortStyle();
	}

	private Context context = new Context();
	private final Stack<Context> contexts = new Stack<>();
	private void saveCurrent() {
		contexts.push(new Context(context));
	}
	private void restoreLatest() {
		context = contexts.pop();
	}

	protected static class Context {
		private Style style;
		private CollStyle collStyle;
		private boolean first;
		private boolean expanded;
		private final IndentLevel indentation;

		public Context() {
			this.first = true;
			this.expanded = true;
			this.indentation = new IndentLevel();
		}
		public Context(Context context) {
			this.style = context.style;
			this.collStyle = context.collStyle;
			this.first = context.first;
			this.expanded = context.expanded;
			this.indentation = new IndentLevel(context.indentation.getLevel());
		}

		public void appendIndent(StringBuilder sb, String indentUnit) {
			for (int i = 0, level = indentation.getLevel(); i < level; ++i) {
				sb.append(indentUnit);
			}
		}

		protected void setStyle(CollStyle collStyle, boolean expanded) {
			this.collStyle = collStyle;
			this.first = true;
			this.expanded = expanded;
		}
	}

	@Override public @Nonnull String toString() {
		if (sb.length() == 0) {
			process(value, toString);
			if (!contexts.isEmpty()) {
				throw new IllegalStateException("Someone didn't end a collection");
			}
		}
		return sb.toString();
	}

	protected <T> void process(@Nullable T value) {
		Stringer<? super T> stringer = repo.findByValue(value);
		process(value, stringer);
	}
	protected <T> void process(@Nullable T value, @Nonnull Stringer<? super T> stringer) {
		begin(value, stringer);
		int start = sb.length();
		int startSize = contexts.size();
		try {
			appendType(stringer.getType(value));
			start = sb.length();
			stringer.toString(this, value);
		} catch (Exception ex) {
			LOG.trace("Cannot process {} via {}", value, stringer, ex);
			sb.setLength(start);
			sb.append(ex.toString());
			while (contexts.size() > startSize) {
				restoreLatest();
			}
		}
		if (contexts.size() < startSize) {
			throw new IllegalStateException("Someone ended too many collections");
		}
		end(value, stringer);
	}
	protected <T> void begin(T object, Stringer<? super T> stringer) {
		saveCurrent();
		context.setStyle(context.style.properties, true);
		if (LOG_ALL) {
			LOG.trace("Starting {} with {}", object, stringer);
		}
	}
	protected <T> void end(T object, Stringer<? super T> stringer) {
		if (LOG_ALL) {
			LOG.trace("Finished {} with {}", object, stringer);
		}
		restoreLatest();
	}

	@Override public void beginSizedList(Object container, int count) {
		beginSizedList(DefaultStringer.debugType(container), count);
	}
	@Override public void beginSizedList(Object container, int count, boolean allowShortcut) {
		beginSizedList(DefaultStringer.debugType(container), count, allowShortcut);
	}
	@Override public void beginSizedList(String containerName, int count) {
		beginSizedList(containerName, count, true);
	}
	@Override public void beginSizedList(String containerName, int count, boolean allowShortcut) {
		prepareItem();
		saveCurrent();
		boolean expanded = count > 1 || (count == 1 && !allowShortcut);
		context.setStyle(context.style.list, expanded);
		sb.append(context.collStyle.beforeHeader);
		sb.append(containerName).append(context.style.sizeInfix).append(count);
		closeHeader(count, expanded);
		beginList();
	}

	@Override public void endSizedList() {
		endList();
		restoreLatest();
	}
	protected void beginList() {
		if (context.expanded) {
			context.indentation.indent();
		}
		sb.append(context.collStyle.begin);
	}

	protected void closeHeader(int count, boolean expanded) {
		if (count < 0) {
			throw new IllegalArgumentException("Count should be non-negative: " + count);
		} else if (count == 0) {
			sb.append(context.collStyle.afterHeaderEmpty);
		} else if (!expanded) {
			sb.append(context.collStyle.afterHeaderShort);
		} else {
			sb.append(context.collStyle.afterHeaderLong);
		}
	}
	protected void beginItem(@Nullable String name) {
		prepareItem();
		if (name != null) {
			sb.append(name);
			sb.append(context.collStyle.keyValueSeparator);
		}
	}
	protected void prepareItem() {
		if (context.first) {
			context.first = false;
			sb.append(context.collStyle.beforeFirstItem);
		} else {
			sb.append(context.collStyle.betweenItems);
		}
		if (context.expanded) {
			context.appendIndent(sb, context.collStyle.indent);
		}
	}
	protected void endItem() {
		sb.append(context.collStyle.endItem);
	}
	protected void endList() {
		sb.append(context.collStyle.afterLastItem);
		sb.append(context.collStyle.end);
	}

	@Override public <T> void item(@Nullable T value) {
		beginItem(null);
		process(value);
		endItem();
	}
	@Override public <T> void item(@Nullable T value, Stringer<? super T> toString) {
		beginItem(null);
		process(value, toString);
		endItem();
	}
	@Override public <T> void item(int index, @Nullable T value) {
		item(String.valueOf(index), value);
	}
	@Override public <T> void item(@Nonnull String name, @Nullable T value) {
		beginItem(name);
		process(value);
		endItem();
	}

	@Override public void identity(@Nullable Object id, @Nullable Object name) {
		if (id != null) {
			sb.append(id);
		}
		if (id != null && name != null) {
			sb.append(context.style.identitySeparator);
		}
		if (name != null) {
			sb.append(name);
		}
	}

	@Override public void beginPropertyGroup(@Nullable String name) {
		prepareItem();
		saveCurrent();
		context.setStyle(context.style.group, true);
		if (name != null) {
			sb.append(context.collStyle.beforeHeader);
			sb.append(name);
			closeHeader(Integer.MAX_VALUE, false);
		}
		beginList();
	}
	@Override public void endPropertyGroup() {
		endList();
		restoreLatest();
	}

	protected void appendType(@Nullable String type) {
		if (type != null) {
			sb.append(context.style.preType);
			sb.append(type);
			sb.append(context.style.postType);
		}
	}

	@Override public <T> void selfDescribingProperty(@Nonnull T value) {
		beginItem(null);
		sb.append(value);
		endItem();
	}
	@Override public void rawProperty(@Nonnull String name, @Nullable Object value) {
		beginItem(name);
		sb.append(value);
		endItem();
	}
	@Override public void typedProperty(@Nonnull String name, @Nullable String type, @Nullable Object value) {
		beginItem(name);
		appendType(type);
		sb.append(value);
		endItem();
	}
	@Override public void formattedProperty(@Nonnull String name, @Nullable String type, String format,
			Object... args) {
		beginItem(name);
		appendType(type);
		f.format(format, args);
		endItem();
	}
	@Override public <T> void complexProperty(@Nonnull String name, @Nullable T value) {
		beginItem(name);
		process(value);
		endItem();
	}
	@Override public <T> void complexProperty(@Nonnull String name, @Nullable T value,
			Stringer<? super T> toString) {
		beginItem(name);
		process(value, toString);
		endItem();
	}
	@Override public void booleanProperty(boolean whether, @Nonnull String positive) {
		beginItem(null);
		if (whether) {
			sb.append(positive);
		} else {
			sb.append(context.style.negatingPrefix).append(positive);
		}
		endItem();
	}
	@Override public void booleanProperty(boolean whether, @Nonnull String positive, @Nonnull String negative) {
		beginItem(null);
		if (whether) {
			sb.append(positive);
		} else {
			sb.append(negative);
		}
		endItem();
	}

	protected static class Style {
		String preType = "(";
		String postType = ")";
		String identitySeparator = ": ";
		String negatingPrefix = "not ";
		String sizeInfix = " of ";
		CollStyle properties = new CollStyle() {
			{
				beforeHeader = "";
				afterHeaderEmpty = "";
				afterHeaderShort = "";
				afterHeaderLong = "";

				begin = "";
				beforeFirstItem = "";
				betweenItems = ", ";
				afterLastItem = "";
				end = "";

				indent = "";
				beginItem = "";
				keyValueSeparator = "=";
				endItem = "";
			}
		};
		CollStyle group = new CollStyle() {
			{
				beforeHeader = "";
				afterHeaderEmpty = "::";
				afterHeaderShort = "::";
				afterHeaderLong = "::";

				begin = "{ ";
				beforeFirstItem = "";
				betweenItems = ", ";
				afterLastItem = "";
				end = " }";

				indent = "";
				beginItem = "";
				keyValueSeparator = "=";
				endItem = "";
			}
		};
		CollStyle list = new CollStyle() {
			{
				beforeHeader = "";
				afterHeaderEmpty = "";
				afterHeaderShort = ": ";
				afterHeaderLong = ":\n";

				begin = "";
				beforeFirstItem = "";
				betweenItems = "\n";
				afterLastItem = "";
				end = "";

				indent = "\t";
				beginItem = "";
				keyValueSeparator = " -> ";
				endItem = "";
			}
		};
	}

	protected static class ShortStyle extends Style {
		ShortStyle() {
			list.afterHeaderShort = "";
			list.afterHeaderLong = "";
			list.betweenItems = ", ";
			list.indent = "";
			list.keyValueSeparator = "->";
			list.begin = "#{";
			list.end = "}";
		}
	}

	protected static class CollStyle {
		String indent;
		String beforeHeader;
		String afterHeaderEmpty;
		String afterHeaderShort;
		String afterHeaderLong;
		String begin;
		String beforeFirstItem;
		String betweenItems;
		String afterLastItem;
		String end;
		String beginItem;
		String keyValueSeparator;
		String endItem;
	}
}
