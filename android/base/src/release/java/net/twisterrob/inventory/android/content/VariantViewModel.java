package net.twisterrob.inventory.android.content;

import java.io.Closeable;

import androidx.annotation.NonNull;

import androidx.lifecycle.ViewModel;

public class VariantViewModel extends ViewModel {
	public VariantViewModel() {
		super();
	}
	public VariantViewModel(@NonNull Closeable... closeables) {
		super(closeables);
	}
}
