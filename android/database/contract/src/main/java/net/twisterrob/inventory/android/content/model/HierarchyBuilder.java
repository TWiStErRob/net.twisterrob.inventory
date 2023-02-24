package net.twisterrob.inventory.android.content.model;

import java.util.*;

import androidx.annotation.*;

import net.twisterrob.inventory.android.content.contract.Type;

public abstract class HierarchyBuilder<B, P extends B, R extends B, I extends B> {
	private final Map<Long, P> properties = new TreeMap<>();
	private final Map<Long, R> rooms = new TreeMap<>();
	private final Map<Long, I> items = new TreeMap<>();

	@SuppressWarnings("unchecked")
	public void preRegister(@NonNull Type type, long id, @NonNull B belonging) {
		switch (type) {
			case Property:
				properties.put(id, (P)belonging);
				break;
			case Room:
				rooms.put(id, (R)belonging);
				break;
			case Item:
				items.put(id, (I)belonging);
				break;
		}
	}

	@SuppressWarnings("unchecked")
	public void put(@NonNull Type parentType, long parentID, @NonNull B child) {
		switch (parentType) {
			case Property: {
				P parent = getOrCreateProperty(parentID);
				addPropertyChild(parent, (R)child);
				break;
			}
			case Room: {
				R parent = getOrCreateRoom(parentID);
				addRoomChild(parent, (I)child);
				break;
			}
			case Item: {
				I parent = getOrCreateItem(parentID);
				addItemChild(parent, (I)child);
				break;
			}
		}
	}
	protected abstract void addPropertyChild(@NonNull P parentProperty, @NonNull R childRoom);
	protected abstract void addRoomChild(@NonNull R parentRoom, @NonNull I childItem);
	protected abstract void addItemChild(@NonNull I parentItem, @NonNull I childItem);

	@SuppressWarnings("unchecked")
	public @Nullable <T extends B> T get(@NonNull Type type, long id) {
		switch (type) {
			case Property:
				return (T)getProperty(id);
			case Room:
				return (T)getRoom(id);
			case Item:
				return (T)getItem(id);
		}
		throw new IllegalStateException("Unknown type: " + type);
	}

	@SuppressWarnings("unchecked")
	public @NonNull <T extends B> T getOrCreate(@NonNull Type type, long id) {
		switch (type) {
			case Property:
				return (T)getOrCreateProperty(id);
			case Room:
				return (T)getOrCreateRoom(id);
			case Item:
				return (T)getOrCreateItem(id);
		}
		throw new IllegalStateException("Unknown type: " + type);
	}

	public @NonNull P getOrCreateProperty(long id) {
		P x = getProperty(id);
		if (x == null) {
			x = createProperty(id);
			properties.put(id, x);
		}
		return x;
	}
	public @Nullable P getProperty(long id) {
		return properties.get(id);
	}
	protected abstract @NonNull P createProperty(long id);

	public @NonNull R getOrCreateRoom(long id) {
		R x = getRoom(id);
		if (x == null) {
			x = createRoom(id);
			rooms.put(id, x);
		}
		return x;
	}
	public @Nullable R getRoom(long id) {
		return rooms.get(id);
	}
	protected abstract @NonNull R createRoom(long id);

	public @NonNull I getOrCreateItem(long id) {
		I x = getItem(id);
		if (x == null) {
			x = createItem(id);
			items.put(id, x);
		}
		return x;
	}
	public @Nullable I getItem(long id) {
		return items.get(id);
	}
	protected abstract @NonNull I createItem(long id);

	public @NonNull Collection<P> getAllProperties() {
		return Collections.unmodifiableCollection(properties.values());
	}
	public @NonNull Collection<R> getAllFooms() {
		return Collections.unmodifiableCollection(rooms.values());
	}
	public @NonNull Collection<I> getAllItems() {
		return Collections.unmodifiableCollection(items.values());
	}
}
