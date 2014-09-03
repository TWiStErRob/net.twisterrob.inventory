package net.twisterrob.inventory.android.utils.glide;

import com.bumptech.glide.load.engine.Resource;

// TODO remove when Glide 3.4.0 is out
public class SimpleResource<T> implements Resource<T> {
	protected final T data;

	public SimpleResource(T data) {
		this.data = data;
	}

	@Override
	public final T get() {
		return data;
	}

	@Override
	public final int getSize() {
		return 1;
	}

	@Override
	public void recycle() {
		// no op
	}
}