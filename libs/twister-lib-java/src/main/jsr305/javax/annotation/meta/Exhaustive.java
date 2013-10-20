package javax.annotation.meta;

import java.lang.annotation.*;

/**
 * This annotation can be applied to the value() element of an annotation that
 * is annotated as a TypeQualifier. This is only appropriate if the value field
 * returns a value that is an {@link java.lang.Enum Enumeration}.
 * 
 * Applications of the type qualifier with different values are exclusive, and
 * the enumeration is an exhaustive list of the possible values.
 * 
 * For example, the following defines a type qualifier such that if you know a
 * value is neither {@code @Foo(Color.Red)} or {@code @Foo(Color.Blue)},
 * then the value must be {@code @Foo(Color.Green)}. And if you know it is
 * {@code @Foo(Color.Green)}, you know it cannot be
 * {@code @Foo(Color.Red)} or {@code @Foo(Color.Blue)}
 * 
 * <pre><code> {@literal @}TypeQualifier
 * {@literal @}interface Foo {
 *     enum Color {RED, BLUE, GREEN};
 *     {@literal @}Exhaustive Color value();
 * }</code></pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Exhaustive {
	// no parameters
}
