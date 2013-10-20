package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.When;

@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckReturnValue {
	When when() default When.ALWAYS;
}
