package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.content.res.Configuration;
import android.support.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		Configuration.KEYBOARD_UNDEFINED,
		Configuration.KEYBOARD_NOKEYS,
		Configuration.KEYBOARD_QWERTY,
		Configuration.KEYBOARD_12KEY
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ConfigurationKeyboard {
	class Converter {
		@DebugHelper
		public static String toString(@ConfigurationKeyboard int kbd) {
			switch (kbd) {
				case Configuration.KEYBOARD_UNDEFINED:
					return "KEYBOARD_UNDEFINED";
				case Configuration.KEYBOARD_NOKEYS:
					return "KEYBOARD_NOKEYS(nokeys)";
				case Configuration.KEYBOARD_QWERTY:
					return "KEYBOARD_QWERTY(qwerty)";
				case Configuration.KEYBOARD_12KEY:
					return "KEYBOARD_12KEY(12key)";
			}
			return "keyboard::" + kbd;
		}
	}
}
