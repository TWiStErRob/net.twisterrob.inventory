package net.twisterrob.test;

import java.util.regex.*;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.*;

import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.java.utils.tostring.stringers.DefaultStringer;

public class PackageNameShortener implements TestRule {
	private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	// non-static for Thread safety, at least in fork mode Class
	private final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");

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
		for (Throwable suppressed : ex.getSuppressed()) {
			transformTraces(suppressed);
		}
	}

	private void fixPackages(Throwable ex) {
		String detailMessage = ReflectionTools.get(ex, "detailMessage");
		detailMessage = fix(detailMessage);
		ReflectionTools.set(ex, "detailMessage", detailMessage);
		ex.getStackTrace();
		//noinspection ConstantConditions if Throwable is messed up, an NPE is in order
		for (StackTraceElement st : ReflectionTools.<StackTraceElement[]>get(ex, "stackTrace")) {
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
