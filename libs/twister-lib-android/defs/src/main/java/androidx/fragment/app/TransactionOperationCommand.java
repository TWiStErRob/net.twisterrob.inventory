package androidx.fragment.app;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		BackStackRecord.OP_NULL,
		BackStackRecord.OP_ADD,
		BackStackRecord.OP_REPLACE,
		BackStackRecord.OP_REMOVE,
		BackStackRecord.OP_HIDE,
		BackStackRecord.OP_SHOW,
		BackStackRecord.OP_DETACH,
		BackStackRecord.OP_ATTACH
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface TransactionOperationCommand {
	class Converter {
		/** @see BackStackRecord#dump */
		@DebugHelper
		public static String toString(@TransactionOperationCommand int cmd) {
			switch (cmd) {
				case BackStackRecord.OP_NULL:
					return "NULL";
				case BackStackRecord.OP_ADD:
					return "ADD";
				case BackStackRecord.OP_REPLACE:
					return "REPLACE";
				case BackStackRecord.OP_REMOVE:
					return "REMOVE";
				case BackStackRecord.OP_HIDE:
					return "HIDE";
				case BackStackRecord.OP_SHOW:
					return "SHOW";
				case BackStackRecord.OP_DETACH:
					return "DETACH";
				case BackStackRecord.OP_ATTACH:
					return "ATTACH";
				default:
					return "cmd::" + cmd;
			}
		}
	}
}
