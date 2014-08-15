package net.twisterrob.inventory.android.content.contract;
public interface CommonColumns {
	String ID = "_id";
	long ID_ADD = Long.MIN_VALUE;

	String NAME = "name"; // never NULL
	String IMAGE = "image";
	String TYPE_IMAGE = "typeImage"; // never NULL
	String COUNT = "childrenCount";
}
