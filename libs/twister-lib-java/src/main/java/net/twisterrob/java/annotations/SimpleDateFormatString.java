package net.twisterrob.java.annotations;

import java.lang.annotation.*;

import javax.annotation.Syntax;
import javax.annotation.meta.*;

/**
 * This qualifier is used to denote String values that should be a format strings accepted by {@link SimpleDateFormatString}.
 */
@Documented
@Syntax("SimpleDateFormat")
@TypeQualifierNickname
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleDateFormatString {
	When when() default When.ALWAYS;

	static class Checker implements TypeQualifierValidator<SimpleDateFormatString> {
		public When forConstantValue(SimpleDateFormatString annotation, Object value) {
			if (!(value instanceof String)) {
				return When.NEVER;
			}

			try {
				@SuppressWarnings("unused")
				java.text.SimpleDateFormat validFormat = new java.text.SimpleDateFormat((String)value);
			} catch (IllegalArgumentException e) {
				return When.NEVER;
			}
			return When.ALWAYS;
		}
	}
}
