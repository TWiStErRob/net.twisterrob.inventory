package net.twisterrob.inventory.database;

import java.io.*;
import java.util.Stack;

import javax.xml.stream.*;

import static javax.xml.stream.XMLStreamConstants.*;

public class DatabaseGenerator {
	private static final String NS = null;

	private final Stack<Category> parents = new Stack<>();
	private final LevelBasedID level = new LevelBasedID();
	private final Printer printer;
	public DatabaseGenerator(Printer printer) {
		this.printer = printer;
	}

	public static void main(String[] args) throws Throwable {
		Reader input = new FileReader(new File("..\\src\\main\\res\\values\\strings_Categories.xml"));
		Writer output = new PrintWriter(System.out, true);
		new DatabaseGenerator(new SQLPrinter()).transform(input, output);
	}

	public void transform(Reader input, Writer output) throws XMLStreamException, IOException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLStreamReader xml = xmlInputFactory.createXMLStreamReader(input);
		printer.start(output);
		while (xml.hasNext()) {
			int type = xml.next();
			if (type == START_ELEMENT && "string".equals(xml.getLocalName())) {
				Category category = parseCategory(xml);
				if (category != null) {
					//System.out.println(); // new category found
					//printAttributes(xml);

					if (category.id == Category.INVALID_ID) {
						category.id = level.newItem(category.level);
					} else if (1000 <= category.id) {
						throw new IllegalStateException("Specific ID cannot be bigger than 1000");
					}
					Category parent;
					while ((parent = parents.empty()? null : parents.peek()) != null
							&& category.level <= parent.level) {
						parents.pop();
					}
					category.setParent(parent);
					printer.print(category, output);
					if (parent == null || parent.level <= category.level) {
						parents.push(category);
					}
				}
			}
		}
		printer.finish(output);
		output.flush();
	}

	private Category parseCategory(XMLStreamReader xml) {
		String name = xml.getAttributeValue(NS, "name");

		if (name == null) {
			throw new IllegalArgumentException("Name is mandatory on a <string>");
		}
		if (name.endsWith("_keywords") || name.endsWith("_description")) {
			return null;
		}

		Category c = new Category();
		c.name = name;

		String level = xml.getAttributeValue(NS, "level");
		if (level != null) {
			c.level = Integer.parseInt(level);
		} else {
			throw new IllegalArgumentException("level is mandatory for a category: " + name);
		}

		String id = xml.getAttributeValue(NS, "id");
		if (id != null) {
			c.id = Integer.parseInt(id);
		}

		c.icon = xml.getAttributeValue(NS, "icon");
		if (c.icon != null && c.icon.startsWith("@raw/")) {
			c.icon = c.icon.substring("@raw/".length());
		}
		return c;
	}

	private void checkCategory(Category category) {
		final File RES_SVG = new File("i:\\src\\main\\res\\raw");
		if (!new File(RES_SVG, category.icon + ".svg").exists()) {
			throw new IllegalArgumentException("Missing icon: " + category);
		}
	}
}
