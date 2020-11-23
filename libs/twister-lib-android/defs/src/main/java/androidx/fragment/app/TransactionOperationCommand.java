package androidx.fragment.app;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		FragmentTransaction.OP_NULL,
		FragmentTransaction.OP_ADD,
		FragmentTransaction.OP_REPLACE,
		FragmentTransaction.OP_REMOVE,
		FragmentTransaction.OP_HIDE,
		FragmentTransaction.OP_SHOW,
		FragmentTransaction.OP_DETACH,
		FragmentTransaction.OP_ATTACH,
		FragmentTransaction.OP_SET_PRIMARY_NAV,
		FragmentTransaction.OP_UNSET_PRIMARY_NAV,
		FragmentTransaction.OP_SET_MAX_LIFECYCLE,
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface TransactionOperationCommand {
	class Converter {
		/** @see BackStackRecord#dump */
		@DebugHelper
		public static String toString(@TransactionOperationCommand int cmd) {
			switch (cmd) {
				case FragmentTransaction.OP_NULL:
					return "NULL";
				case FragmentTransaction.OP_ADD:
					return "ADD";
				case FragmentTransaction.OP_REPLACE:
					return "REPLACE";
				case FragmentTransaction.OP_REMOVE:
					return "REMOVE";
				case FragmentTransaction.OP_HIDE:
					return "HIDE";
				case FragmentTransaction.OP_SHOW:
					return "SHOW";
				case FragmentTransaction.OP_DETACH:
					return "DETACH";
				case FragmentTransaction.OP_ATTACH:
					return "ATTACH";
				case FragmentTransaction.OP_SET_PRIMARY_NAV:
					return "SET_PRIMARY_NAV";
				case FragmentTransaction.OP_UNSET_PRIMARY_NAV:
					return "UNSET_PRIMARY_NAV";
				case FragmentTransaction.OP_SET_MAX_LIFECYCLE:
					return "SET_MAX_LIFECYCLE";
				default:
					return "cmd::" + cmd;
			}
		}
	}
}
