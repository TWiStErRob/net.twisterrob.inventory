package net.twisterrob.build;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class Transform {
	public static void main(String... args) throws IOException, TransformerException {
		FileInputStream xml = new FileInputStream(args[0]);
		FileInputStream xslt = new FileInputStream(args[1]);
		FileOutputStream out = new FileOutputStream(args[2]);
		out.write(0xEF);
		out.write(0xBB);
		out.write(0xBF);

		long startFactory = System.currentTimeMillis();
		TransformerFactory factory = TransformerFactory.newInstance();
		long endFactory = System.currentTimeMillis();
		long startTransformer = System.currentTimeMillis();
		Transformer transformer = factory.newTransformer(new StreamSource(xslt));
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
	}
}
