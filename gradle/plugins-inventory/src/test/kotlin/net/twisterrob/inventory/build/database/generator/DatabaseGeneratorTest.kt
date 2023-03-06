package net.twisterrob.inventory.build.database.generator

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DatabaseGeneratorTest {

	@Test fun `internal category with id minus one`() {
		val xml = """
			<resources>
				<string name="category_internal" id="-1" level="0">text</string>1
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		assertThat(
			result, hasItems(
				Category(name = "category_internal", id = -1, level = 0),
			)
		)
	}

	@Test fun `internal category with id zero`() {
		val xml = """
			<resources>
				<string name="category_internal" id="0" level="0">text</string>1
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		assertThat(
			result, hasItems(
				Category(name = "category_internal", id = 0, level = 0),
			)
		)
	}

	@Test fun `explicit top level IDs`() {
		val xml = """
			<resources>
				<string name="category_one" id="1" level="0">text</string>
				<string name="category_two" id="2" level="0">text</string>
				<string name="category_three" id="3" level="0">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		assertThat(
			result, hasItems(
				Category(name = "category_one", id = 1, level = 0),
				Category(name = "category_two", id = 2, level = 0),
				Category(name = "category_three", id = 3, level = 0),
			)
		)
	}

	@Test fun `auto-generated top level category id`() {
		val xml = """
			<resources>
				<string name="category_one" level="0">text</string>
				<string name="category_two" level="0">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		assertThat(
			result, hasItems(
				Category(name = "category_one", id = 1000, level = 0),
				Category(name = "category_two", id = 2000, level = 0),
			)
		)
	}

	@Test fun `auto-generated nested categories - level 2`() {
		val xml = """
			<resources>
				<string name="category_one" level="0">text</string>
				<string name="category_one_one" level="1">text</string>
				<string name="category_one_two" level="1">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		val one = Category(parent = null, name = "category_one", id = 1000, level = 0)
		val oneOne = Category(parent = one, name = "category_one_one", id = 1100, level = 1)
		val oneTwo = Category(parent = one, name = "category_one_two", id = 1200, level = 1)
		assertThat(result, hasItems(one, oneOne, oneTwo))
	}

	@Test fun `auto-generated nested categories - level 3`() {
		val xml = """
			<resources>
				<string name="category_1" level="0">text</string>
				<string name="category_11" level="1">text</string>
				<string name="category_111" level="2">text</string>
				<string name="category_112" level="2">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		val c1 = Category(parent = null, name = "category_1", id = 1000, level = 0)
		val c11 = Category(parent = c1, name = "category_11", id = 1100, level = 1)
		val c111 = Category(parent = c11, name = "category_111", id = 1110, level = 2)
		val c112 = Category(parent = c11, name = "category_112", id = 1120, level = 2)
		assertThat(result, hasItems(c1, c11, c111, c112))
	}

	@Test fun `sibling categories`() {
		val xml = """
			<resources>
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

		val result = renderAsCategories(xml)

		val c1 = Category(parent = null, name = "category_1", id = 1000, level = 0)
		val c11 = Category(parent = c1, name = "category_11", id = 1100, level = 1)
		val c111 = Category(parent = c11, name = "category_111", id = 1110, level = 2)
		val c112 = Category(parent = c11, name = "category_112", id = 1120, level = 2)
		val c12 = Category(parent = c1, name = "category_12", id = 1200, level = 1)
		val c121 = Category(parent = c12, name = "category_121", id = 1210, level = 2)
		val c122 = Category(parent = c12, name = "category_122", id = 1220, level = 2)

		assertThat(result, hasItems(c1, c11, c111, c112, c12, c121, c122))
	}

	@Test fun `keywords strings are ignored`() {
		val xml = """
			<resources>
				<string name="category_uncategorized_keywords">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		assertThat(result.size, equalTo(0))
	}

	@Test fun `description strings are ignored`() {
		val xml = """
			<resources>
				<string name="category_uncategorized_description">text</string>
			</resources>
		""".trimIndent()

		val result = renderAsCategories(xml)

		assertThat(result.size, equalTo(0))
	}

	@Test(expected = IllegalStateException::class)
	fun `other strings fail`() {
		val xml = """
			<resources>
				<string name="category_uncategorized">text</string>
			</resources>
		""".trimIndent()

		renderAsCategories(xml)
	}
}
