package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

@Documented
@TypeQualifierNickname
@Untainted(when = When.ALWAYS)
@Retention(RetentionPolicy.RUNTIME)
public @interface Detainted {
	// no parameters
}
