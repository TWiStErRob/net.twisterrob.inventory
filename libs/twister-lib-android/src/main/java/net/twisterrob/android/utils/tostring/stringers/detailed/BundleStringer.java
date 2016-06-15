package net.twisterrob.android.utils.tostring.stringers.detailed;

import android.os.Bundle;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tostring.Stringer;
import net.twisterrob.android.utils.tostring.stringers.map.*;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public class BundleStringer implements Stringer<Bundle> {
	@Override public @NonNull String toString(Bundle bundle) {
		// FIXME ToStringCanvas canvas = ToStringCanvas.SHORT.start();
		MapStringer canvas = MapStringer.LONG.start();
		canvas.toStringRec(new BundleDeepCollection(bundle), false);
		return canvas.finish();
	}
}
