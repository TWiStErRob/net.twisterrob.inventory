package javax.annotation.meta;

import java.lang.annotation.*;

/**
 * This annotation is applied to a annotation, and marks the annotation as being
 * a qualifier nickname. Applying a nickname annotation {@code X} to a element {@code Y} should
 * be interpreted as having the same meaning as applying all of annotations of {@code X}
 * (other than {@link TypeQualifierNickname}) to {@code Y}.
 * 
 * <p>Thus, you might define a qualifier <code>SocialSecurityNumber</code> as follows:</p>
 * 
 * <pre><code> {@literal @}Documented
 * {@literal @}Retention(RetentionPolicy.RUNTIME)
 * {@literal @}TypeQualifierNickname
 * {@literal @}Pattern("[0-9]{3}-[0-9]{2}-[0-9]{4}") 
 * public {@literal @}interface SocialSecurityNumber {
 *     // no parameters
 * }</code></pre>
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface TypeQualifierNickname {
	// no parameters
}
