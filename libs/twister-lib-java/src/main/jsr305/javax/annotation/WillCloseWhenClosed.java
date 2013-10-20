package javax.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
/**
 * Used to annotate a constructor/factory parameter to indicate that returned
 * object (X) will close the resource when X is closed.
 */
public @interface WillCloseWhenClosed {
	// no parameters
}
