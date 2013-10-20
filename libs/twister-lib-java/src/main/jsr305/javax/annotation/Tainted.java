package javax.annotation;

import java.lang.annotation.*;

import javax.annotation.meta.*;

@Documented
@TypeQualifierNickname
@Untainted(when = When.MAYBE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tainted {
	// no parameters
}
