package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.lang.reflect.*;

import javax.annotation.Nonnull;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.os.Build.*;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class PendingIntentStringer extends Stringer<PendingIntent> {
	private static final Logger LOG = LoggerFactory.getLogger(PendingIntentStringer.class);

	/**
	 * @since API VERSION_CODES.JELLY_BEAN_MR2
	 * @until API VERSION_CODES.N https://stackoverflow.com/q/42401911/253468
	 */
	private static final Method getIntent = ReflectionTools.tryFindDeclaredMethod(PendingIntent.class, "getIntent");
	/** @since API 16 */
	private static final Method isActivity = ReflectionTools.tryFindDeclaredMethod(PendingIntent.class, "isActivity");

	@Override public void toString(@Nonnull ToStringAppender append, PendingIntent pending) {
		append.identity(pending, null);
		try {
			if (isActivity != null) {
				append.booleanProperty((Boolean)isActivity.invoke(pending), "activity");
			}
			if (getIntent != null) {
				try {
					append.complexProperty("intent", getIntent.invoke(pending));
				} catch (InvocationTargetException ex) {
					append.rawProperty("intent", ex.getCause().toString());
				}
			}
		} catch (Exception ex) {
			LOG.warn("Cannot inspect PendingIntent", ex);
		}
	}
}
