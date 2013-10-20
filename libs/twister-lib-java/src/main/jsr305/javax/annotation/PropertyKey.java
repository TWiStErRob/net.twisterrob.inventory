package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

@Documented
@TypeQualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyKey {
	When when() default When.ALWAYS;
}
