package net.twisterrob.java.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * Annotate methods that shouldn't be used in production,
 * and don't use anything annotated with this for anything else other than temporary development debugging or logging.
 * Needs to go to the class file so ProGuard can use it.
 */
@Retention(RetentionPolicy.CLASS)
@Target({CONSTRUCTOR, METHOD, TYPE, PACKAGE})
public @interface DebugHelper {
}
