package javax.annotation.meta;

/**
 * Used to describe the relationship between a qualifier {@code T} and the set of values
 * {@code S} possible on an annotated element.
 * 
 * In particular, an issues should be reported if an {@link #ALWAYS} or {@link #MAYBE} value is
 * used where a {@link #NEVER} value is required, or if a {@link #NEVER} or {@link #MAYBE} value is used
 * where an {@link #ALWAYS} value is required.
 */
public enum When {
	/** {@code S} is a subset of {@code T} */
	ALWAYS,
	/** nothing definitive is known about the relation between {@code S} and {@code T} */
	UNKNOWN,
	/** {@code S} intersection {@code T} is non empty and {@code S} - {@code T} is nonempty */
	MAYBE,
	/** {@code S} intersection {@code T} is empty */
	NEVER;
}
