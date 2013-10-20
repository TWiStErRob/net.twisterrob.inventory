package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

/** Used to annotate a value of unknown sign */

@Documented
@TypeQualifierNickname
@Nonnegative(when = When.UNKNOWN)
@Retention(RetentionPolicy.RUNTIME)
public @interface Signed {
	// no parameters
}
