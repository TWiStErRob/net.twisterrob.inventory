package net.twisterrob.android.test;

import java.util.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.event.EventConstants;

import net.twisterrob.java.utils.ObjectTools;

/**
 * <pre><code>
 * doAnswer(new LoggingAnswer<>(LOG)).when(mock).call(args);
 * when(mock.call(args)).thenAnswer(new LoggingAnswer(LOG));
 * </code></pre>
 */
public class LoggingAnswer<T> implements Answer<T> {
	private final Logger log;
	private final int level;

	private static final Set<Integer> VALID_LEVELS = new HashSet<>(Arrays.asList(
			EventConstants.TRACE_INT,
			EventConstants.DEBUG_INT,
			EventConstants.INFO_INT,
			EventConstants.WARN_INT,
			EventConstants.ERROR_INT
	));

	public LoggingAnswer(Logger log) {
		this(log, EventConstants.TRACE_INT);
	}

	/**
	 * @param level log event level
	 * @see EventConstants
	 * @see EventConstants#TRACE_INT TRACE_INT
	 * @see EventConstants#DEBUG_INT DEBUG_INT
	 * @see EventConstants#INFO_INT INFO_INT
	 * @see EventConstants#WARN_INT WARN_INT
	 * @see EventConstants#ERROR_INT ERROR_INT
	 * @see org.slf4j.spi.LocationAwareLogger
	 */
	public LoggingAnswer(Logger log, int level) {
		this.log = ObjectTools.checkNotNull(log);
		if (!VALID_LEVELS.contains(level)) {
			throw new IllegalArgumentException(
					"Invalid level: " + level + ", must be one of " + VALID_LEVELS + "; see " + EventConstants.class);
		}
		this.level = level;
	}

	@Override public T answer(InvocationOnMock invocation) {
		String message = invocation.toString();

		switch (level) {
			case EventConstants.TRACE_INT:
				log.trace(message);
				break;
			case EventConstants.DEBUG_INT:
				log.debug(message);
				break;
			case EventConstants.INFO_INT:
				log.info(message);
				break;
			case EventConstants.WARN_INT:
				log.warn(message);
				break;
			case EventConstants.ERROR_INT:
				log.error(message);
				break;
			default:
				throw new IllegalStateException("Level (" + level + ")not recognized for " + invocation);
		}

		return null;
	}
}
