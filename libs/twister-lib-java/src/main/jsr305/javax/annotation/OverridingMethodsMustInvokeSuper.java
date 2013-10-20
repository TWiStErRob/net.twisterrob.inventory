package javax.annotation;

import java.lang.annotation.*;

/**
 * When this annotation is applied to a method, it indicates that if this method
 * is overridden in a subclass, the overriding method should invoke this method
 * (through method invocation on super).
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OverridingMethodsMustInvokeSuper {
	// no parameters
}
