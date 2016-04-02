package net.twisterrob.android.utils.log;

import org.slf4j.Logger;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.java.annotations.DebugHelper;

@DebugHelper
public interface LoggingDebugProvider {
	@SuppressWarnings("RedundantThrows")
	String getDebugInfo() throws Throwable;

	class LoggingHelper {
		/** <code>name.method(args[0], ..., args[n]):\ndebugInfo()</code> */
		static void log(Logger LOG, String name, String method, LoggingDebugProvider debugInfoProvider,
				Object... args) {
			Throwable t = null;
			StringBuilder message = new StringBuilder();
			message.append(name).append(".").append(method).append("(");
			for (int i = 0; i < args.length; i++) {
				message.append(AndroidTools.toString(args[i]));
				if (i < args.length - 1) {
					message.append(", ");
				}
			}
			message.append(")");
			if (debugInfoProvider != null) {
				String info;
				try {
					info = debugInfoProvider.getDebugInfo();
				} catch (Throwable ex) {
					info = ex.toString();
					t = ex;
				}
				message.append(":\n").append(info);
			}

			LOG.trace(message.toString(), t);
		}
	}
}
