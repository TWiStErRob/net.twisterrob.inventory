package net.twisterrob.java.io;

import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.*;

public class IndentingXMLStreamWriter implements XMLStreamWriter {
	private final XMLStreamWriter writer;

	private enum State {
		NONE,
		ELEMENT,
		DATA
	}

	private State state;
	private final Stack<State> states;
	private int depth;

	public IndentingXMLStreamWriter(XMLStreamWriter writer) {
		this.writer = writer;
		state = State.NONE;
		states = new Stack<>();
		depth = 0;
	}

	private void printIndent() throws XMLStreamException {
		if (depth > 0) {
			for (int i = 0; i < depth; i++) {
				writer.writeCharacters("\t");
			}
		}
	}

	private void onEmptyElement() throws XMLStreamException {
		state = State.ELEMENT;
		if (depth > 0) {
			writer.writeCharacters("\n");
		}
		printIndent();
	}

	private void onStartElement() throws XMLStreamException {
		states.push(State.ELEMENT);
		state = State.NONE;
		if (depth > 0) {
			writer.writeCharacters("\n");
		}
		printIndent();
		depth++;
	}

	private void onEndElement() throws XMLStreamException {
		depth--;
		if (state == State.ELEMENT) {
			writer.writeCharacters("\n");
			printIndent();
		}
		state = states.pop();
	}

	public void writeStartDocument() throws XMLStreamException {
		writer.writeStartDocument();
		writer.writeCharacters("\n");
	}

	public void writeStartDocument(String s) throws XMLStreamException {
		writer.writeStartDocument(s);
		writer.writeCharacters("\n");
	}

	public void writeStartDocument(String s, String s1) throws XMLStreamException {
		writer.writeStartDocument(s, s1);
		writer.writeCharacters("\n");
	}

	public void writeStartElement(String s) throws XMLStreamException {
		onStartElement();
		writer.writeStartElement(s);
	}

	public void writeStartElement(String s, String s1) throws XMLStreamException {
		onStartElement();
		writer.writeStartElement(s, s1);
	}

	public void writeStartElement(String s, String s1, String s2) throws XMLStreamException {
		onStartElement();
		writer.writeStartElement(s, s1, s2);
	}

	public void writeEmptyElement(String s, String s1) throws XMLStreamException {
		onEmptyElement();
		writer.writeEmptyElement(s, s1);
	}

	public void writeEmptyElement(String s, String s1, String s2) throws XMLStreamException {
		onEmptyElement();
		writer.writeEmptyElement(s, s1, s2);
	}

	public void writeEmptyElement(String s) throws XMLStreamException {
		onEmptyElement();
		writer.writeEmptyElement(s);
	}

	public void writeEndElement() throws XMLStreamException {
		onEndElement();
		writer.writeEndElement();
	}

	public void writeCharacters(String s) throws XMLStreamException {
		state = State.DATA;
		writer.writeCharacters(s);
	}

	public void writeCharacters(char ac[], int i, int j) throws XMLStreamException {
		state = State.DATA;
		writer.writeCharacters(ac, i, j);
	}

	public void writeCData(String s) throws XMLStreamException {
		state = State.DATA;
		writer.writeCData(s);
	}

	public Object getProperty(String s) throws IllegalArgumentException {
		return writer.getProperty(s);
	}

	public NamespaceContext getNamespaceContext() {
		return writer.getNamespaceContext();
	}

	public void setNamespaceContext(NamespaceContext namespacecontext) throws XMLStreamException {
		writer.setNamespaceContext(namespacecontext);
	}

	public void setDefaultNamespace(String s) throws XMLStreamException {
		writer.setDefaultNamespace(s);
	}

	public void setPrefix(String s, String s1) throws XMLStreamException {
		writer.setPrefix(s, s1);
	}

	public String getPrefix(String s) throws XMLStreamException {
		return writer.getPrefix(s);
	}

	public void writeEntityRef(String s) throws XMLStreamException {
		writer.writeEntityRef(s);
	}

	public void writeDTD(String s) throws XMLStreamException {
		writer.writeDTD(s);
	}

	public void writeProcessingInstruction(String s, String s1) throws XMLStreamException {
		writer.writeProcessingInstruction(s, s1);
	}

	public void writeProcessingInstruction(String s) throws XMLStreamException {
		writer.writeProcessingInstruction(s);
	}

	public void writeComment(String s) throws XMLStreamException {
		writer.writeComment(s);
	}

	public void writeDefaultNamespace(String s) throws XMLStreamException {
		writer.writeDefaultNamespace(s);
	}

	public void writeNamespace(String s, String s1) throws XMLStreamException {
		writer.writeNamespace(s, s1);
	}

	public void writeAttribute(String s, String s1, String s2) throws XMLStreamException {
		writer.writeAttribute(s, s1, s2);
	}

	public void writeAttribute(String s, String s1, String s2, String s3) throws XMLStreamException {
		writer.writeAttribute(s, s1, s2, s3);
	}

	public void writeAttribute(String s, String s1) throws XMLStreamException {
		writer.writeAttribute(s, s1);
	}

	public void flush() throws XMLStreamException {
		writer.flush();
	}

	public void close() throws XMLStreamException {
		writer.close();
	}

	public void writeEndDocument() throws XMLStreamException {
		writer.writeEndDocument();
	}
}
