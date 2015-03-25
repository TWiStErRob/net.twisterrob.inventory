package net.twisterrob.java.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.SOURCE)
@Target({CONSTRUCTOR, METHOD, TYPE, PACKAGE})
public @interface DebugHelper {
}
