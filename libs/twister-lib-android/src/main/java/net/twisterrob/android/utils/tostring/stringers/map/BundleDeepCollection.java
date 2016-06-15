package net.twisterrob.android.utils.tostring.stringers.map;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.*;

import android.os.Bundle;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.java.collections.*;
import net.twisterrob.java.utils.CollectionTools;

public class BundleDeepCollection implements DeepCollection {
	private static final Logger LOG = LoggerFactory.getLogger(BundleDeepCollection.class);

	private final Bundle bundle;
	private final String errored;

	public BundleDeepCollection(Bundle bundle) {
		this.bundle = bundle;

		String error = null;
		try {
			bundle.size(); // force unparcel
		} catch (Exception ex) {
			LOG.error("Cannot unparcel Bundle for logging", ex);
			error = ex.toString();
		}
		this.errored = error;
	}

	@Override public Iterator<Entry<?, ?>> iterator() {
		if (hasErrored()) {
			return new ConstantIterator<Entry<?, ?>>(new Entry<String, String>() {
				@Override public String getKey() {
					return "!ERROR!";
				}
				@Override public String getValue() {
					return errored;
				}
				@Override public String setValue(String object) {
					throw new UnsupportedOperationException();
				}
			});
		}
		final TreeSet<String> sortedKeys =
				CollectionTools.newTreeSet(bundle.keySet(), new NullsSafeComparator<String>());
		return new Iterator<Entry<?, ?>>() {
			private final Iterator<String> it = sortedKeys.iterator();
			private String current;

			@Override public boolean hasNext() {
				return it.hasNext();
			}
			@Override public Entry<?, ?> next() {
				current = it.next();
				return entry;
			}
			@Override public void remove() {
				throw new UnsupportedOperationException();
			}
			private final Entry<String, Object> entry = new Entry<String, Object>() {
				@Override public String getKey() {
					return current;
				}
				@Override public Object getValue() {
					return bundle.get(getKey());
				}
				@Override public Object setValue(Object object) {
					throw new UnsupportedOperationException();
				}
			};
		};
	}
	@Override public String getType() {
		return AndroidTools.debugType(bundle);
	}
	@Override public int size() {
		return hasErrored()? 1 : bundle.size();
	}
	private boolean hasErrored() {
		return errored != null;
	}
}
