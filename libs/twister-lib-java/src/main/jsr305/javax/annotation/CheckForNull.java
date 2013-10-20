package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

@Documented
@TypeQualifierNickname
@Nonnull(when = When.MAYBE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckForNull {
	// no parameters
}
