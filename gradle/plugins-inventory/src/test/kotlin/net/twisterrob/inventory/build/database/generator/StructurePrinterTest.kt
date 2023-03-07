package net.twisterrob.inventory.build.database.generator

import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("LongMethod")
class StructurePrinterTest {

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

		val result = renderAsText(StructurePrinter(), xml)

		assertEquals(
			"""
				0  /    -1	category_internal, icon='null'
				0  /     0	category_0, icon='null'
				0  /  1000	category_1, icon='null'
				1  /  1100		category_11, icon='null'
				2  /  1110			category_111, icon='null'
				2  /  1120			category_112, icon='null'
				1  /  1200		category_12, icon='null'
				2  /  1210			category_121, icon='null'
				2  /  1220			category_122, icon='null'
				0  /  2000	category_2, icon='null'
				1  /  2100		category_21, icon='null'
				2  /  2110			category_211, icon='null'
				2  /  2120			category_212, icon='null'
				1  /  2200		category_22, icon='null'
				2  /  2210			category_221, icon='null'
				2  /  2220			category_222, icon='null'
				
			""".trimIndent(),
			result
		)
	}

	@Test fun `markers on high category counts`() {
		val xml = """
			<resources>
				<string name="category_1" level="0">text</string>
				
				<string name="category_11" level="1">text</string>
				<string name="category_111" level="2">text</string>
				<string name="category_1111" level="3">text</string>
				<string name="category_1112" level="3">text</string>
				<string name="category_1113" level="3">text</string>
				<string name="category_1114" level="3">text</string>
				<string name="category_1115" level="3">text</string>
				<string name="category_1116" level="3">text</string>
				<string name="category_1117" level="3">text</string>
				<string name="category_1118" level="3">text</string>
				<string name="category_1119" level="3">text</string>
				<string name="category_112" level="2">text</string>
				<string name="category_113" level="2">text</string>
				<string name="category_114" level="2">text</string>
				<string name="category_115" level="2">text</string>
				<string name="category_116" level="2">text</string>
				<string name="category_117" level="2">text</string>
				<string name="category_118" level="2">text</string>
				<string name="category_119" level="2">text</string>
				
				<string name="category_12" level="1">text</string>
				<string name="category_13" level="1">text</string>
				<string name="category_14" level="1">text</string>
				<string name="category_15" level="1">text</string>
				<string name="category_16" level="1">text</string>
				<string name="category_17" level="1">text</string>
				<string name="category_18" level="1">text</string>
				<string name="category_19" level="1">text</string>
				
				<string name="category_2" level="0">text</string>
				<string name="category_3" level="0">text</string>
				<string name="category_4" level="0">text</string>
				<string name="category_5" level="0">text</string>
				<string name="category_6" level="0">text</string>
				<string name="category_7" level="0">text</string>
				<string name="category_8" level="0">text</string>
				<string name="category_9" level="0">text</string>
				<string name="category_10" level="0">text</string>
				<string name="category_11" level="0">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsText(StructurePrinter(), xml)

		assertEquals(
			"""
				0  /  1000	category_1, icon='null'
				1  /  1100		category_11, icon='null'
				2  /  1110			category_111, icon='null'
				3  /  1111				category_1111, icon='null'
				3  /  1112				category_1112, icon='null'
				3  /  1113				category_1113, icon='null'
				3  /  1114				category_1114, icon='null'
				3  /  1115				category_1115, icon='null'
				3  /  1116				category_1116, icon='null'
				3  /  1117				category_1117, icon='null'
				3* /  1118				category_1118, icon='null'
				3* /  1119				category_1119, icon='null'
				2  /  1120			category_112, icon='null'
				2  /  1130			category_113, icon='null'
				2  /  1140			category_114, icon='null'
				2  /  1150			category_115, icon='null'
				2  /  1160			category_116, icon='null'
				2  /  1170			category_117, icon='null'
				2* /  1180			category_118, icon='null'
				2* /  1190			category_119, icon='null'
				1  /  1200		category_12, icon='null'
				1  /  1300		category_13, icon='null'
				1  /  1400		category_14, icon='null'
				1  /  1500		category_15, icon='null'
				1  /  1600		category_16, icon='null'
				1  /  1700		category_17, icon='null'
				1* /  1800		category_18, icon='null'
				1* /  1900		category_19, icon='null'
				0  /  2000	category_2, icon='null'
				0  /  3000	category_3, icon='null'
				0  /  4000	category_4, icon='null'
				0  /  5000	category_5, icon='null'
				0  /  6000	category_6, icon='null'
				0  /  7000	category_7, icon='null'
				0  /  8000	category_8, icon='null'
				0  /  9000	category_9, icon='null'
				0  / 10000	category_10, icon='null'
				0  / 11000	category_11, icon='null'
				
			""".trimIndent(),
			result
		)
	}
}
