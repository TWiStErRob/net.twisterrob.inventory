package net.twisterrob.inventory.android.content;

import java.io.Closeable;

import androidx.annotation.NonNull;

import net.twisterrob.android.utils.log.LoggingViewModel;

public class VariantViewModel extends LoggingViewModel {
	public VariantViewModel() {
		super();
	}
	public VariantViewModel(@NonNull Closeable... closeables) {
		super(closeables);
	}
}
