package net.twisterrob.test;

import java.util.regex.*;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.*;
import org.mockito.exceptions.base.MockitoException;

import android.os.Build.*;

import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

/**
 * Note: this uses reflection, so there's a need to open up Java with:
 * <pre><code>
 * --add-opens java.base/java.lang=ALL-UNNAMED
 * </code></pre>
 */
public class PackageNameShortener implements TestRule {
	private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	private static final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");

	@Override public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} catch (Throwable ex) {
					transformTraces(ex);
					throw ex;
				}
			}
		};
	}

	private void transformTraces(Throwable ex) {
		if (ex == null) {
			return;
		}
		fixPackages(ex);
		if (ex.getCause() != null) {
			transformTraces(ex.getCause());
		} else {
			if (ex instanceof MultipleFailureException) {
				for (Throwable failure : ((MultipleFailureException)ex).getFailures()) {
					transformTraces(failure);
				}
			}
		}
		if (VERSION_CODES.KITKAT <= VERSION.SDK_INT) {
			for (Throwable suppressed : ex.getSuppressed()) {
				transformTraces(suppressed);
			}
		}
	}

	private void fixPackages(Throwable ex) {
		String detailMessage = ReflectionTools.get(ex, "detailMessage");
		detailMessage = fix(detailMessage);
		ReflectionTools.set(ex, "detailMessage", detailMessage);
		for (StackTraceElement st : ex.getStackTrace()) {
			String className = ReflectionTools.get(st, "declaringClass");
			className = fix(className);
			ReflectionTools.set(st, "declaringClass", className);
		}
	}

	private String fix(String detailMessage) {
		if (detailMessage == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		Matcher matcher = FQCN.matcher(detailMessage);
		while (matcher.find()) {
			String shorter = DefaultStringer.shortenPackageNames(matcher.group());
			matcher.appendReplacement(sb, Matcher.quoteReplacement(shorter));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
