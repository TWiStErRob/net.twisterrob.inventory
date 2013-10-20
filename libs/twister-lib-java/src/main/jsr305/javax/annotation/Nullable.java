package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

@Documented
@TypeQualifierNickname
@Nonnull(when = When.UNKNOWN)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nullable {
	// no parameters
}
