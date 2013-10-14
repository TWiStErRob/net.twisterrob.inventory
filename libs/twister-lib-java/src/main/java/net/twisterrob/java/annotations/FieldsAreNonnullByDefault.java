package net.twisterrob.java.annotations;

import java.lang.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Applies the {@link Nonnull} annotation to every class field unless overridden.
 */
@Documented
@Nonnull
@TypeQualifierDefault(ElementType.FIELD)
// <-- use METHOD for return values
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsAreNonnullByDefault {
	// nothing to add
}
