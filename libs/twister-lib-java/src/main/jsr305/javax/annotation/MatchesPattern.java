package javax.annotation;

import java.lang.annotation.*;
import java.util.regex.Pattern;

import javax.annotation.meta.*;

@Documented
@TypeQualifier(applicableTo = String.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MatchesPattern {
	@RegEx
	String value();

	int flags() default 0;

	static class Checker implements TypeQualifierValidator<MatchesPattern> {
		public When forConstantValue(MatchesPattern annotation, Object value) {
			Pattern p = Pattern.compile(annotation.value(), annotation.flags());
			if (p.matcher(((String)value)).matches()) {
				return When.ALWAYS;
			}
			return When.NEVER;
		}
	}
}
