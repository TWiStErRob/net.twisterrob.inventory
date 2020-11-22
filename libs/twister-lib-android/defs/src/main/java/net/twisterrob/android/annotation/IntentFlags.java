package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.*;
import android.content.Intent;
import android.os.Build.VERSION_CODES;

import static android.content.Intent.*;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressWarnings("deprecation")
@SuppressLint({"InlinedApi", "UniqueConstants"})
@TargetApi(VERSION_CODES.LOLLIPOP)
@IntDef(flag = true, value = {
		FLAG_GRANT_READ_URI_PERMISSION,
		FLAG_GRANT_WRITE_URI_PERMISSION,
		FLAG_FROM_BACKGROUND,
		FLAG_DEBUG_LOG_RESOLUTION,
		FLAG_EXCLUDE_STOPPED_PACKAGES,
		FLAG_INCLUDE_STOPPED_PACKAGES,
		FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
		FLAG_GRANT_PREFIX_URI_PERMISSION,
		FLAG_ACTIVITY_NO_HISTORY,
		FLAG_ACTIVITY_SINGLE_TOP,
		FLAG_ACTIVITY_NEW_TASK,
		FLAG_ACTIVITY_MULTIPLE_TASK,
		FLAG_ACTIVITY_CLEAR_TOP,
		FLAG_ACTIVITY_FORWARD_RESULT,
		FLAG_ACTIVITY_PREVIOUS_IS_TOP,
		FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
		FLAG_ACTIVITY_BROUGHT_TO_FRONT,
		FLAG_ACTIVITY_RESET_TASK_IF_NEEDED,
		FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY,
		FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET,
		FLAG_ACTIVITY_NEW_DOCUMENT,
		FLAG_ACTIVITY_NO_USER_ACTION,
		FLAG_ACTIVITY_REORDER_TO_FRONT,
		FLAG_ACTIVITY_NO_ANIMATION,
		FLAG_ACTIVITY_CLEAR_TASK,
		FLAG_ACTIVITY_TASK_ON_HOME,
		FLAG_ACTIVITY_RETAIN_IN_RECENTS,
		FLAG_RECEIVER_REGISTERED_ONLY,
		FLAG_RECEIVER_REPLACE_PENDING,
		FLAG_RECEIVER_FOREGROUND,
		FLAG_RECEIVER_NO_ABORT,
		0x04000000, // @hide FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
		0x02000000, // @hide FLAG_RECEIVER_BOOT_UPGRADE
})
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface IntentFlags {
	class Converter {
		/** @see Intent#IMMUTABLE_FLAGS */
		@SuppressWarnings("JavadocReference" /* @hide IMMUTABLE_FLAGS */)
		private static final int IMMUTABLE_FLAGS = FLAG_GRANT_READ_URI_PERMISSION
				| FLAG_GRANT_WRITE_URI_PERMISSION | FLAG_GRANT_PERSISTABLE_URI_PERMISSION
				| FLAG_GRANT_PREFIX_URI_PERMISSION;

		/** @see Intent */
		@DebugHelper
		public static String toString(@IntentFlags int flags, Boolean isActivity) {
			StringBuilder sb = new StringBuilder();
			sb.append("0x").append(Integer.toHexString(flags));
			sb.append("[");

			int f = flags;
			f = handleFlag(sb, f, FLAG_GRANT_READ_URI_PERMISSION, "FLAG_GRANT_READ_URI_PERMISSION");
			f = handleFlag(sb, f, FLAG_GRANT_WRITE_URI_PERMISSION, "FLAG_GRANT_WRITE_URI_PERMISSION");
			f = handleFlag(sb, f, FLAG_FROM_BACKGROUND, "FLAG_FROM_BACKGROUND");
			f = handleFlag(sb, f, FLAG_DEBUG_LOG_RESOLUTION, "FLAG_DEBUG_LOG_RESOLUTION");
			f = handleFlag(sb, f, FLAG_EXCLUDE_STOPPED_PACKAGES, "FLAG_EXCLUDE_STOPPED_PACKAGES");
			f = handleFlag(sb, f, FLAG_INCLUDE_STOPPED_PACKAGES, "FLAG_INCLUDE_STOPPED_PACKAGES");
			f = handleFlag(sb, f, FLAG_GRANT_PERSISTABLE_URI_PERMISSION, "FLAG_GRANT_PERSISTABLE_URI_PERMISSION");
			f = handleFlag(sb, f, FLAG_GRANT_PREFIX_URI_PERMISSION, "FLAG_GRANT_PREFIX_URI_PERMISSION");
			if (isActivity == null || isActivity) {
				f = handleActivityFlags(sb, f);
			}
			if (isActivity == null || !isActivity) {
				f = handleReceiverFlags(sb, f);
			}

			if (f != 0) {
				sb.append(" and remainder: 0x").append(Integer.toHexString(f));
			}
			sb.append("]");
			return sb.toString();
		}

		private static int handleActivityFlags(StringBuilder sb, int f) {
			f = handleFlag(sb, f, FLAG_ACTIVITY_NO_HISTORY, "FLAG_ACTIVITY_NO_HISTORY");
			f = handleFlag(sb, f, FLAG_ACTIVITY_SINGLE_TOP, "FLAG_ACTIVITY_SINGLE_TOP");
			f = handleFlag(sb, f, FLAG_ACTIVITY_NEW_TASK, "FLAG_ACTIVITY_NEW_TASK");
			f = handleFlag(sb, f, FLAG_ACTIVITY_MULTIPLE_TASK, "FLAG_ACTIVITY_MULTIPLE_TASK");
			f = handleFlag(sb, f, FLAG_ACTIVITY_CLEAR_TOP, "FLAG_ACTIVITY_CLEAR_TOP");
			f = handleFlag(sb, f, FLAG_ACTIVITY_FORWARD_RESULT, "FLAG_ACTIVITY_FORWARD_RESULT");
			f = handleFlag(sb, f, FLAG_ACTIVITY_PREVIOUS_IS_TOP, "FLAG_ACTIVITY_PREVIOUS_IS_TOP");
			f = handleFlag(sb, f, FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
			f = handleFlag(sb, f, FLAG_ACTIVITY_BROUGHT_TO_FRONT, "FLAG_ACTIVITY_BROUGHT_TO_FRONT");
			f = handleFlag(sb, f, FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
			f = handleFlag(sb, f, FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY, "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
			f = handleFlag(sb, f, FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
			f = handleFlag(sb, f, FLAG_ACTIVITY_NEW_DOCUMENT, "=FLAG_ACTIVITY_NEW_DOCUMENT"); // same as prev!
			f = handleFlag(sb, f, FLAG_ACTIVITY_NO_USER_ACTION, "FLAG_ACTIVITY_NO_USER_ACTION");
			f = handleFlag(sb, f, FLAG_ACTIVITY_REORDER_TO_FRONT, "FLAG_ACTIVITY_REORDER_TO_FRONT");
			f = handleFlag(sb, f, FLAG_ACTIVITY_NO_ANIMATION, "FLAG_ACTIVITY_NO_ANIMATION");
			f = handleFlag(sb, f, FLAG_ACTIVITY_CLEAR_TASK, "FLAG_ACTIVITY_CLEAR_TASK");
			f = handleFlag(sb, f, FLAG_ACTIVITY_TASK_ON_HOME, "FLAG_ACTIVITY_TASK_ON_HOME");
			f = handleFlag(sb, f, FLAG_ACTIVITY_RETAIN_IN_RECENTS, "FLAG_ACTIVITY_RETAIN_IN_RECENTS");
			return f;
		}

		@SuppressLint("WrongConstant")
		private static int handleReceiverFlags(StringBuilder sb, int f) {
			f = handleFlag(sb, f, FLAG_RECEIVER_REGISTERED_ONLY, "FLAG_RECEIVER_REGISTERED_ONLY");
			f = handleFlag(sb, f, FLAG_RECEIVER_REPLACE_PENDING, "FLAG_RECEIVER_REPLACE_PENDING");
			f = handleFlag(sb, f, FLAG_RECEIVER_FOREGROUND, "FLAG_RECEIVER_FOREGROUND");
			f = handleFlag(sb, f, FLAG_RECEIVER_NO_ABORT, "FLAG_RECEIVER_NO_ABORT");
			f = handleFlag(sb, f, 0x04000000, "FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT");
			f = handleFlag(sb, f, 0x02000000, "FLAG_RECEIVER_BOOT_UPGRADE");
			return f;
		}

		private static @IntentFlags int handleFlag(StringBuilder sb,
				@IntentFlags int flags, @IntentFlags int flag, String flagName) {
			if ((flags & flag) == flag) {
				flags &= ~flag;
				if (sb.charAt(sb.length() - 1) != '[') {
					sb.append(" | ");
				}
				sb.append(flagName);
				if ((IMMUTABLE_FLAGS & flag) == flag) {
					sb.append('*');
				}
			}
			return flags;
		}
	}
}
