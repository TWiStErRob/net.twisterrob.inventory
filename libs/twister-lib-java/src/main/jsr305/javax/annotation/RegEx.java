package javax.annotation;

import java.lang.annotation.*;
import java.util.regex.*;

import javax.annotation.meta.*;

/**
 * This qualifier is used to denote String values that should be a Regular expression.
 */
@Documented
@Syntax("RegEx")
@TypeQualifierNickname
@Retention(RetentionPolicy.RUNTIME)
public @interface RegEx {
	When when() default When.ALWAYS;

	static class Checker implements TypeQualifierValidator<RegEx> {
		public When forConstantValue(RegEx annotation, Object value) {
			if (!(value instanceof String)) {
				return When.NEVER;
			}

			try {
				Pattern.compile((String)value);
			} catch (PatternSyntaxException e) {
				return When.NEVER;
			}
			return When.ALWAYS;
		}
	}
}
