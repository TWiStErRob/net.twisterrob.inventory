package net.twisterrob.android.utils.tostring.stringers.detailed;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import org.slf4j.*;

import android.app.PendingIntent;

import net.twisterrob.java.annotations.DebugHelper;
import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.java.utils.tostring.*;

@DebugHelper
public class PendingIntentStringer extends Stringer<PendingIntent> {
	private static final Logger LOG = LoggerFactory.getLogger(PendingIntentStringer.class);

	private static final Method getIntent = ReflectionTools.tryFindDeclaredMethod(PendingIntent.class, "getIntent");
	private static final Method isActivity = ReflectionTools.tryFindDeclaredMethod(PendingIntent.class, "isActivity");

	@Override public void toString(@Nonnull ToStringAppender append, PendingIntent pending) {
		append.identity(pending, null);
		try {
			append.booleanProperty((Boolean)isActivity.invoke(pending), "activity");
			append.complexProperty("intent", getIntent.invoke(pending));
		} catch (Exception ex) {
			LOG.warn("Cannot inspect PendingIntent", ex);
		}
	}
}
