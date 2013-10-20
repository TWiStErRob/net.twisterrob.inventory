package javax.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
/**
 * Used to annotate a method parameter to indicate that this method will close
 * the resource.
 */
public @interface WillClose {
	// no parameters
}
