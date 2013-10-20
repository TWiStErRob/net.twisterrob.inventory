package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

/**
 * The annotated element must not be <code>null</code>.
 * Annotated fields must not be <code>null</code> after construction has completed.
 * Annotated methods must have non-<code>null</code> return values.
 * @see <a href="http://findbugs.sourceforge.net/manual/annotations.html">FindBugs documentation: Annotations</a>
 */
@Documented
@TypeQualifier
// @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nonnull {
	When when() default When.ALWAYS;

	static class Checker implements TypeQualifierValidator<Nonnull> {
		public When forConstantValue(Nonnull qualifierqualifierArgument, Object value) {
			if (value == null) {
				return When.NEVER;
			}
			return When.ALWAYS;
		}
	}
}
