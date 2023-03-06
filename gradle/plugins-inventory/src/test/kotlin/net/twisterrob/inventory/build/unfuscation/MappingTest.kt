package net.twisterrob.inventory.build.unfuscation

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MappingTest {

	@Rule
	@JvmField
	val temp = TemporaryFolder()

	@Test
	fun `multiple mapped classes`() {
		val input = temp.newFile()
		val output = temp.newFile()
		input.writeText(
			"""
				org.slf4j.helpers.NOPLogger -> org.a.b.d:
				    org.slf4j.helpers.NOPLogger NOP_LOGGER -> a
				    48:49:void <init>() -> <init>
				    55:55:java.lang.String getName() -> a
				    42:42:void <clinit>() -> <clinit>
				org.slf4j.helpers.NOPLoggerFactory -> org.a.b.e:
				    40:42:void <init>() -> <init>
				    45:45:org.slf4j.Logger getLogger(java.lang.String) -> a
			""".trimIndent()
		)

		Mapping().remap(input, output)

		assertEquals(
			"""
				org.slf4j.helpers.NOPLogger -> org.slf4j.helpers.NOPLogger_d:
				    org.slf4j.helpers.NOPLogger NOP_LOGGER -> NOP_LOGGER_a
				    48:49:void <init>() -> <init>
				    55:55:java.lang.String getName() -> getName_a
				    42:42:void <clinit>() -> <clinit>
				org.slf4j.helpers.NOPLoggerFactory -> org.slf4j.helpers.NOPLoggerFactory_e:
				    40:42:void <init>() -> <init>
				    45:45:org.slf4j.Logger getLogger(java.lang.String) -> getLogger_a
				
			""".trimIndent(),
			output.readText()
		)
	}

	@Test
	fun `shortened some package names`() {
		val input = temp.newFile()
		val output = temp.newFile()
		input.writeText(
			"""
				org.slf4j.helpers.NOPLogger -> org.a.b.d:
				    55:55:java.lang.String getName() -> a
			""".trimIndent()
		)

		Mapping().remap(input, output)

		assertEquals(
			"""
				org.slf4j.helpers.NOPLogger -> org.slf4j.helpers.NOPLogger_d:
				    55:55:java.lang.String getName() -> getName_a
				
			""".trimIndent(), output.readText()
		)
	}

	@Test
	fun `shortened only classname`() {
		val input = temp.newFile()
		val output = temp.newFile()
		input.writeText(
			"""
				androidx.activity.OnBackPressedCallback -> androidx.activity.c:
				    boolean mEnabled -> a
			""".trimIndent()
		)

		Mapping().remap(input, output)

		assertEquals(
			"""
				androidx.activity.OnBackPressedCallback -> androidx.activity.OnBackPressedCallback_c:
				    boolean mEnabled -> mEnabled_a
				
			""".trimIndent(), output.readText()
		)
	}

	@Test
	fun `shortened to default package`() {
		val input = temp.newFile()
		val output = temp.newFile()
		input.writeText(
			"""
				androidx.activity.OnBackPressedCallback$2 -> a:
				    boolean mEnabled -> a
			""".trimIndent()
		)

		Mapping().remap(input, output)

		assertEquals(
			"""
				androidx.activity.OnBackPressedCallback$2 -> androidx.activity.OnBackPressedCallback$2_a:
				    boolean mEnabled -> mEnabled_a
				
			""".trimIndent(), output.readText()
		)
	}

	@Test
	fun `all names are kept`() {
		val input = temp.newFile()
		val output = temp.newFile()
		input.writeText(
			"""
				net.twisterrob.inventory.android.activity.MainActivity -> net.twisterrob.inventory.android.activity.MainActivity:
				    java.util.Map TITLES -> TITLES
				    boolean isInventoryEmptyCache -> isInventoryEmptyCache
				    46:46:void <init>() -> <init>
				    85:113:void onCreate(android.os.Bundle) -> onCreate
			""".trimIndent()
		)

		Mapping().remap(input, output)

		assertEquals(
			"""
				net.twisterrob.inventory.android.activity.MainActivity -> net.twisterrob.inventory.android.activity.MainActivity:
				    java.util.Map TITLES -> TITLES
				    boolean isInventoryEmptyCache -> isInventoryEmptyCache
				    46:46:void <init>() -> <init>
				    85:113:void onCreate(android.os.Bundle) -> onCreate
				
		""".trimIndent(), output.readText()
		)
	}
}
