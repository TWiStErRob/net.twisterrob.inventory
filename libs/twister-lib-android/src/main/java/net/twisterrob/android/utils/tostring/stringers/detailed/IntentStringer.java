package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build.*;

import net.twisterrob.android.annotation.IntentFlags;
import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
@TargetApi(VERSION_CODES.JELLY_BEAN)
public class IntentStringer extends Stringer<Intent> {
	@Override public String getType(Intent object) {
		return null;
	}
	@Override public void toString(@Nonnull ToStringAppender append, Intent intent) {
		append.beginSizedList(intent, 10, false); // TODO figure out a better way
		append.item("pkg", intent.getPackage());
		append.item("cmp", intent.getComponent());
		//noinspection ResourceType TOFIX external annotations to Intent#getFlags?
		append.item("flg", IntentFlags.Converter.toString(intent.getFlags(), null));

		append.item("dat", intent.getData());
		append.item("typ", intent.getType());

		append.item("act", intent.getAction());
		append.item("cat", intent.getCategories());

		append.item("bnds", intent.getSourceBounds());

		append.item("xtra", intent.getExtras());
		if (VERSION_CODES.JELLY_BEAN <= VERSION.SDK_INT) {
			append.item("clip", intent.getClipData());
		}
		append.endSizedList();
	}
}
