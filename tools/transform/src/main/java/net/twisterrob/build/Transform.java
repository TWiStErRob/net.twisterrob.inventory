package net.twisterrob.build;

import java.io.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import net.twisterrob.build.GatheringErrorListener.Problem;
import net.twisterrob.build.GatheringErrorListener.Problem.Severity;

public class Transform {
	public static void main(String... args) throws IOException, TransformerException {
		FileInputStream xml = new FileInputStream(args[0]);
		FileInputStream xslt = new FileInputStream(args[1]);
		FileOutputStream out = new FileOutputStream(args[2]);
		out.write(0xEF);
		out.write(0xBB);
		out.write(0xBF);

		GatheringErrorListener listener = new GatheringErrorListener();

		long startFactory = System.currentTimeMillis();
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setErrorListener(listener);
		long endFactory = System.currentTimeMillis();
		long startTransformer = System.currentTimeMillis();
		Transformer transformer = factory.newTransformer(new StreamSource(xslt));
		transformer.setErrorListener(listener);
		long endTransformer = System.currentTimeMillis();
		System.out.printf("Transforming %s into %s through %s using %s created by %s%n",
				args[0], args[2], args[1], transformer, factory);
		transformer.setOutputProperty("{http://xml.apache.org/xalan}entities", "org/apache/xml/serializer/XMLEntities");
		long startTransform = System.currentTimeMillis();
		transformer.transform(new StreamSource(xml), new StreamResult(out));
		long endTransform = System.currentTimeMillis();

		long timeFactory = endFactory - startFactory;
		long timeTransformer = endTransformer - startTransformer;
		long timeTransform = endTransform - startTransform;
		long timeSetup = timeFactory + timeTransformer;
		long timeTotal = timeSetup + timeTransform;
		System.out.printf("Transformation took %.3f seconds (setup = %d ms, transform = %d ms).%n",
				timeTotal / 1000d, timeSetup, timeTransform);

		if (!listener.problems.isEmpty()) {
			StringBuilder message = new StringBuilder("Transformation had problems:\n");
			for (Problem problem : listener.problems) {
				message.append(problem.severity)
				       .append(": ")
				       .append(problem.exception)
				       .append(" at ")
				       .append(problem.exception.getLocationAsString());
			}
			TransformerException ex = new TransformerException(message.toString());
			if (listener.problems.size() < 10) {
				for (Problem problem : listener.problems) {
					ex.addSuppressed(problem.exception);
				}
			}
			throw ex;
		}
	}
}

class GatheringErrorListener implements ErrorListener {
	public final List<Problem> problems = new ArrayList<>();

	@Override public void warning(TransformerException exception) {
		problems.add(new Problem(Severity.warning, exception));
	}
	@Override public void error(TransformerException exception) {
		problems.add(new Problem(Severity.error, exception));
	}
	@Override public void fatalError(TransformerException exception) {
		problems.add(new Problem(Severity.fatalError, exception));
	}

	static class Problem {
		public final Severity severity;
		public final TransformerException exception;

		Problem(Severity severity, TransformerException exception) {
			this.severity = severity;
			this.exception = exception;
		}

		enum Severity {
			warning,
			error,
			fatalError
		}
	}
}
