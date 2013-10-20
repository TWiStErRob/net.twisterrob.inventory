package javax.annotation.meta;

import java.lang.annotation.*;

/**
 * This qualifier is applied to an annotation to denote that the annotation
 * defines a default type qualifier that is visible within the scope of the
 * element it is applied to.
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeQualifierDefault {
	ElementType[] value() default {};
}
