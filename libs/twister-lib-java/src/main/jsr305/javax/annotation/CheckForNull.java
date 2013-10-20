package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

/**
 * The annotated element might be <code>null</code>, and uses of the element should check for <code>null</code>.
 * When this annotation is applied to a method it applies to the method return value.
 * @see <a href="http://findbugs.sourceforge.net/manual/annotations.html">FindBugs documentation: Annotations</a>
 */
@Documented
@TypeQualifierNickname
@Nonnull(when = When.MAYBE)
//@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckForNull {
	// no parameters
}
