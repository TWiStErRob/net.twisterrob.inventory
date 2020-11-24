package androidx.drawerlayout.widget;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout.SavedState;

import net.twisterrob.android.annotation.*;
import net.twisterrob.java.utils.tostring.*;

public class DrawerLayoutStateStringer extends Stringer<SavedState> {
	@Override public void toString(@NonNull ToStringAppender append, SavedState state) {
		append.rawProperty("OpenDrawer", GravityFlag.Converter.toString(state.openDrawerGravity));
		append.beginPropertyGroup("LockMode");
		append.rawProperty("left", LockMode.Converter.toString(state.lockModeLeft));
		append.rawProperty("right", LockMode.Converter.toString(state.lockModeRight));
		append.rawProperty("start", LockMode.Converter.toString(state.lockModeStart));
		append.rawProperty("end", LockMode.Converter.toString(state.lockModeEnd));
		append.endPropertyGroup();
	}
}
