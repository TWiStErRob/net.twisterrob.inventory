package net.twisterrob.inventory.android.utils;

import com.google.android.gms.common.api.GoogleApiClient;

public interface ApiClientProvider {
	GoogleApiClient getConnectedClient();
}