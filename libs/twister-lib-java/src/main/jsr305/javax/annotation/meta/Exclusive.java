package javax.annotation.meta;

import java.lang.annotation.*;

/**
 * This annotation can be applied to the value() element of an annotation that is annotated as a {@link TypeQualifier}.
 * 
 * For example, the following defines a type qualifier such that if you know a
 * value is {@code @Foo(1)}, then the value cannot be {@code @Foo(2)} or {@code @Foo(3)}.
 * 
 * <pre><code> {@literal @}TypeQualifier
 * {@literal @}interface Foo {
 *     {@literal @}Exclusive int value();
 * }</code></pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Exclusive {
	// no parameters
}
