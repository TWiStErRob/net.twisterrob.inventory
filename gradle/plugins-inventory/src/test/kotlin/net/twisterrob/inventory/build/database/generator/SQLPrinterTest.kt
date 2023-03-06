package net.twisterrob.inventory.build.database.generator;

import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("LongMethod")
class SQLPrinterTest {

	@Test fun `top-level`() {
		val xml = """
			<resources>
				<string name="category_internal" id="-1" level="0">text</string>
				<string name="category_0" id="0" level="0">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsText(SQLPrinter(), xml)

		@Language("SQLITE-SQL")
		val expected = """
			-- The following INSERT INTOs are generated via 'gradlew generateDB'
			
			INSERT INTO Category
				           (parent,   _id, name,                                      image)
				      SELECT  NULL,    -1, 'category_internal',                       'null'
				UNION SELECT  NULL,     0, 'category_0',                              'null'
			;
			
		""".trimIndent()
		assertEquals(expected, result)
	}

	@Test fun `multiple levels with debug`() {
		val xml = """
			<resources>
				<string name="category_1" level="0">text</string>
				
				<string name="category_11" level="1">text</string>
				<string name="category_111" level="2">text</string>
				<string name="category_112" level="2">text</string>
				
				<string name="category_12" level="1">text</string>
				<string name="category_121" level="2">text</string>
				<string name="category_122" level="2">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsText(SQLPrinter(debug = true), xml)

		@Language("SQLITE-SQL")
		val expected = """
			-- The following INSERT INTOs are generated via 'gradlew generateDB'
			
			INSERT INTO Category
				           (parent,   _id, name,                                      image)
				      SELECT  NULL,  1000, 'category_1',                              'null' -- 0
			
				UNION SELECT  1000,  1100, 'category_11',                             'null' -- 1
				UNION SELECT  1100,  1110,     'category_111',                        'null' -- 2
				UNION SELECT  1100,  1120,     'category_112',                        'null' -- 2
			
				UNION SELECT  1000,  1200, 'category_12',                             'null' -- 1
				UNION SELECT  1200,  1210,     'category_121',                        'null' -- 2
				UNION SELECT  1200,  1220,     'category_122',                        'null' -- 2
			;
			
		""".trimIndent()
		assertEquals(expected, result)
	}
	
	@Test fun `multiple levels with siblings`() {
		val xml = """
			<resources>
				<string name="category_internal" id="-1" level="0">text</string>
				<string name="category_0" id="0" level="0">text</string>
				
				<string name="category_1" level="0">text</string>
				
				<string name="category_11" level="1">text</string>
				<string name="category_111" level="2">text</string>
				<string name="category_112" level="2">text</string>
				
				<string name="category_12" level="1">text</string>
				<string name="category_121" level="2">text</string>
				<string name="category_122" level="2">text</string>
				
				<string name="category_2" level="0">text</string>
				
				<string name="category_21" level="1">text</string>
				<string name="category_211" level="2">text</string>
				<string name="category_212" level="2">text</string>
				
				<string name="category_22" level="1">text</string>
				<string name="category_221" level="2">text</string>
				<string name="category_222" level="2">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsText(SQLPrinter(), xml)

		@Language("SQLITE-SQL")
		val expected = """
			-- The following INSERT INTOs are generated via 'gradlew generateDB'
			
			INSERT INTO Category
				           (parent,   _id, name,                                      image)
				      SELECT  NULL,    -1, 'category_internal',                       'null'
				UNION SELECT  NULL,     0, 'category_0',                              'null'
			;
			INSERT INTO Category
				           (parent,   _id, name,                                      image)
				      SELECT  NULL,  1000, 'category_1',                              'null'
			
				UNION SELECT  1000,  1100, 'category_11',                             'null'
				UNION SELECT  1100,  1110,     'category_111',                        'null'
				UNION SELECT  1100,  1120,     'category_112',                        'null'
			
				UNION SELECT  1000,  1200, 'category_12',                             'null'
				UNION SELECT  1200,  1210,     'category_121',                        'null'
				UNION SELECT  1200,  1220,     'category_122',                        'null'
			;
			INSERT INTO Category
				           (parent,   _id, name,                                      image)
				      SELECT  NULL,  2000, 'category_2',                              'null'
			
				UNION SELECT  2000,  2100, 'category_21',                             'null'
				UNION SELECT  2100,  2110,     'category_211',                        'null'
				UNION SELECT  2100,  2120,     'category_212',                        'null'
			
				UNION SELECT  2000,  2200, 'category_22',                             'null'
				UNION SELECT  2200,  2210,     'category_221',                        'null'
				UNION SELECT  2200,  2220,     'category_222',                        'null'
			;
			
		""".trimIndent()
		assertEquals(expected, result)
	}
}
