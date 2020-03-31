package net.twisterrob.build;

import java.io.*;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.xml.sax.*;

import net.twisterrob.build.GatheringErrorListener.Problem;
import net.twisterrob.build.GatheringErrorListener.Problem.Severity;

public class Transform {
	public static void main(String... args) throws IOException, TransformerException, SAXException {
		FileInputStream xmlToTransform = new FileInputStream(args[0]);
		FileInputStream xmlToValidate = new FileInputStream(args[0]);
		FileInputStream xsd = new FileInputStream(args[1]);
		FileInputStream xslt = new FileInputStream(args[2]);
		FileOutputStream out = new FileOutputStream(args[3]);
		out.write(0xEF);
		out.write(0xBB);
		out.write(0xBF);

		long startSchemaFactory = System.currentTimeMillis();
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		long endSchemaFactory = System.currentTimeMillis();
		long startSchemaValidator = System.currentTimeMillis();
		Schema schema = schemaFactory.newSchema(new StreamSource(xsd));
		Validator validator = schema.newValidator();
		long endSchemaValidator = System.currentTimeMillis();
		System.out.printf("Validating %s with %s using %s through %s created by %s%n",
				args[0], args[1], schema, validator, schemaFactory);
		validator.setErrorHandler(new ErrorHandler() {
			@Override public void warning(SAXParseException exception) throws SAXException {
				throw exception;
			}
			@Override public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}
			@Override public void fatalError(SAXParseException exception) throws SAXException {
				throw exception;
			}
		});
		long startValidation = System.currentTimeMillis();
		validator.validate(new StreamSource(xmlToValidate));
		long endValidation = System.currentTimeMillis();

		long timeSchemaFactory = endSchemaFactory - startSchemaFactory;
		long timeSchemaValidator = endSchemaValidator - startSchemaValidator;
		long timeValidation = endValidation - startValidation;
		long timeSchemaSetup = timeSchemaFactory + timeSchemaValidator;
		long timeSchemaTotal = timeSchemaSetup + timeValidation;
		System.out.printf("Validation took %.3f seconds (setup = %d ms, validation = %d ms).%n",
				timeSchemaTotal / 1000d, timeSchemaSetup, timeValidation);

		GatheringErrorListener listener = new GatheringErrorListener();

		long startFactory = System.currentTimeMillis();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setErrorListener(listener);
		long endFactory = System.currentTimeMillis();
		long startTransformer = System.currentTimeMillis();
		Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslt));
		transformer.setErrorListener(listener);
		long endTransformer = System.currentTimeMillis();
		System.out.printf("Transforming %s into %s through %s using %s created by %s%n",
				args[0], args[3], args[2], transformer, transformerFactory);
		transformer.setOutputProperty("{http://xml.apache.org/xalan}entities",
				"org/apache/xml/serializer/XMLEntities");
		long startTransform = System.currentTimeMillis();
		transformer.transform(new StreamSource(xmlToTransform), new StreamResult(out));
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
