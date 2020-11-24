package net.twisterrob.inventory.android.activity;

import android.content.*;
import android.os.Bundle;

import androidx.annotation.*;

import net.twisterrob.inventory.android.preferences.R;

public class PreferencesActivity extends BaseActivity {
	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_activity);
	}

	public static @NonNull Intent show(@NonNull Context context) {
		return new Intent(context, PreferencesActivity.class);
	}
}
