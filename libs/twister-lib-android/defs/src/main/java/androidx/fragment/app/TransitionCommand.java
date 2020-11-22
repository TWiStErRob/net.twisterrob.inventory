package androidx.fragment.app;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		FragmentTransaction.TRANSIT_UNSET,
		FragmentTransaction.TRANSIT_NONE,
		FragmentTransaction.TRANSIT_FRAGMENT_OPEN,
		FragmentTransaction.TRANSIT_FRAGMENT_CLOSE,
		FragmentTransaction.TRANSIT_FRAGMENT_FADE
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface TransitionCommand {
	class Converter {
		/** @see FragmentTransaction */
		@DebugHelper
		public static String toString(@TransitionCommand int transition) {
			switch (transition) {
				case FragmentTransaction.TRANSIT_UNSET:
					return "TRANSIT_UNSET";
				case FragmentTransaction.TRANSIT_NONE:
					return "TRANSIT_NONE";
				case FragmentTransaction.TRANSIT_FRAGMENT_OPEN:
					return "TRANSIT_FRAGMENT_OPEN";
				case FragmentTransaction.TRANSIT_FRAGMENT_CLOSE:
					return "TRANSIT_FRAGMENT_CLOSE";
				case FragmentTransaction.TRANSIT_FRAGMENT_FADE:
					return "TRANSIT_FRAGMENT_FADE";
			}
			return "transition::" + transition;
		}
	}
}
