package net.twisterrob.java.utils.tostring;

import javax.annotation.Nonnull;

import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ToStringerTest {
	private static final Object PLACEHOLDER = new Object();

	private StringerRepo repo;

	@Before public void setup() {
		repo = new StringerRepo();
	}

	@Test public void testToString_ValueCached() {
		Stringer<Object> stringer = spy(new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.selfDescribingProperty("value");
			}
		});
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, stringer);
		assertEquals("(Object)value", toString.toString());
		reset((Object)stringer);
		assertEquals("(Object)value", toString.toString());
		verifyZeroInteractions(stringer);
	}

	@Test public void testRawProperty_Single() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.rawProperty("key", "value");
			}
		});
		assertEquals("(Object)key=value", toString.toString());
	}

	@Test public void testTypedProperty_Single() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.typedProperty("key1", "type1", "value1");
			}
		});
		assertEquals("(Object)key1=(type1)value1", toString.toString());
	}

	@Test public void testSelfDescribingProperty_Single() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.selfDescribingProperty("value1");
			}
		});
		assertEquals("(Object)value1", toString.toString());
	}

	@Test public void testBooleanProperty_Single_Negative() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.booleanProperty(false, "positive");
			}
		});
		assertEquals("(Object)not positive", toString.toString());
	}

	@Test public void testBooleanProperty_Single_Positive() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.booleanProperty(true, "positive");
			}
		});
		assertEquals("(Object)positive", toString.toString());
	}

	@Test public void tesFormattedProperty_Single() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.formattedProperty("key1", "type1", "format1 %s %s", "arg1", "arg2");
			}
		});
		assertEquals("(Object)key1=(type1)format1 arg1 arg2", toString.toString());
	}

	@Test public void testBooleanProperty_Single_Positive_specified() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.booleanProperty(true, "positive", "negative");
			}
		});
		assertEquals("(Object)positive", toString.toString());
	}

	@Test public void testBooleanProperty_Single_Negative_specified() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.booleanProperty(false, "positive", "negative");
			}
		});
		assertEquals("(Object)negative", toString.toString());
	}

	@Test public void testRawProperties_MultipleInGroup() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginPropertyGroup("group");
				append.rawProperty("key1", "value1");
				append.rawProperty("key2", "value2");
				append.rawProperty("key3", "value3");
				append.endPropertyGroup();
			}
		});
		assertEquals("(Object)group::{ key1=value1, key2=value2, key3=value3 }", toString.toString());
	}

	@Test public void testRawProperties_MultipleGroups() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginPropertyGroup("group1");
				append.rawProperty("key1", "value1");
				append.rawProperty("key2", "value2");
				append.endPropertyGroup();
				append.beginPropertyGroup("group2");
				append.rawProperty("key3", "value3");
				append.rawProperty("key4", "value4");
				append.endPropertyGroup();
				append.beginPropertyGroup("group3");
				append.rawProperty("key5", "value5");
				append.endPropertyGroup();
			}
		});
		assertEquals("(Object)"
						+ "group1::{ key1=value1, key2=value2 }"
						+ ", group2::{ key3=value3, key4=value4 }"
						+ ", group3::{ key5=value5 }"
				, toString.toString());
	}

	@Test public void testRawProperties_Multiple() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.rawProperty("key1", "value1");
				append.rawProperty("key2", "value2");
				append.rawProperty("key3", "value3");
			}
		});
		assertEquals("(Object)key1=value1, key2=value2, key3=value3"
				, toString.toString());
	}

	@Test public void testProperties_Mixed() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.rawProperty("rawKey", "rawValue");
				append.selfDescribingProperty("selfDescribing");
				append.typedProperty("typedKey", "typedType", "typedValue");
				append.booleanProperty(true, "positive", "negative");
				append.beginPropertyGroup("group");
				append.selfDescribingProperty("selfDescribingInGroup");
				append.endPropertyGroup();
				append.formattedProperty("formattedKey", "formattedType", "%s %02d %.3f", "formatArg", 2, 3.0);
				append.complexProperty("complexKey", "complexValue");
			}
		});
		assertEquals("(Object)"
						+ "rawKey=rawValue"
						+ ", selfDescribing"
						+ ", typedKey=(typedType)typedValue"
						+ ", positive"
						+ ", group::{ selfDescribingInGroup }"
						+ ", formattedKey=(formattedType)formatArg 02 3.000"
						+ ", complexKey=\"complexValue\""
				, toString.toString());
	}

	@Test public void testList() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public String getType(Object object) {
				return null;
			}
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginSizedList("Test", 3);
				append.item("key1", "value1");
				append.item("key2", 2.0);
				append.item("key3", (byte)3);
				append.endSizedList();
			}
		});
		assertEquals("Test of 3:\n"
						+ "\tkey1 -> \"value1\"\n"
						+ "\tkey2 -> (Double)2.0\n"
						+ "\tkey3 -> (Byte)3"
				, toString.toString());
	}
	@Test public void testList_Nested() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public String getType(Object object) {
				return null;
			}
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginSizedList("Test", 5);
				{
					append.beginSizedList("Test1", 2);
					append.item("value1.1");
					append.item("value1.2");
					append.endSizedList();
				}
				append.item("key2", "value2");
				{
					append.beginSizedList("Test2", 2);
					append.item("key2.1", "value2.1");
					append.item("key2.2", "value2.2");
					append.endSizedList();
				}
				{
					append.beginSizedList("Test3", 2);
					append.item(31, "value3.1");
					append.item(32, "value3.2");
					append.endSizedList();
				}
				append.item("key5", (short)5);
				append.endSizedList();
			}
		});
		assertEquals("Test of 5:\n"
						+ "\tTest1 of 2:\n"
						+ "\t\t\"value1.1\"\n"
						+ "\t\t\"value1.2\"\n"
						+ "\tkey2 -> \"value2\"\n"
						+ "\tTest2 of 2:\n"
						+ "\t\tkey2.1 -> \"value2.1\"\n"
						+ "\t\tkey2.2 -> \"value2.2\"\n"
						+ "\tTest3 of 2:\n"
						+ "\t\t31 -> \"value3.1\"\n"
						+ "\t\t32 -> \"value3.2\"\n"
						+ "\tkey5 -> (Short)5"
				, toString.toString());
	}

	@Test public void testList_Nested_Objects() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public String getType(Object object) {
				return null;
			}
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginSizedList("Test", 5);
				append.item(PLACEHOLDER, new Stringer<Object>() {
					@Override public void toString(@Nonnull ToStringAppender append, Object object) {
						append.beginSizedList("Test1", 2);
						append.item("value1.1");
						append.item("value1.2");
						append.endSizedList();
					}
				});
				append.item("key2", "value2");
				append.item(PLACEHOLDER, new Stringer<Object>() {
					@Override public void toString(@Nonnull ToStringAppender append, Object object) {
						append.beginSizedList("Test2", 2);
						append.item("key2.1", "value2.1");
						append.item("key2.2", "value2.2");
						append.endSizedList();
					}
				});
				append.item(PLACEHOLDER, new Stringer<Object>() {
					@Override public void toString(@Nonnull ToStringAppender append, Object object) {
						append.beginSizedList("Test3", 2);
						append.item(31, "value3.1");
						append.item(32, "value3.2");
						append.endSizedList();
					}
				});
				append.item("key5", (short)5);
				append.endSizedList();
			}
		});
		assertEquals("Test of 5:\n"
						+ "\t(Object)Test1 of 2:\n"
						+ "\t\t\"value1.1\"\n"
						+ "\t\t\"value1.2\"\n"
						+ "\tkey2 -> \"value2\"\n"
						+ "\t(Object)Test2 of 2:\n"
						+ "\t\tkey2.1 -> \"value2.1\"\n"
						+ "\t\tkey2.2 -> \"value2.2\"\n"
						+ "\t(Object)Test3 of 2:\n"
						+ "\t\t31 -> \"value3.1\"\n"
						+ "\t\t32 -> \"value3.2\"\n"
						+ "\tkey5 -> (Short)5"
				, toString.toString());
	}

	@Test public void testList_Nested_Multilevel() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public String getType(Object object) {
				return null;
			}
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginSizedList("Test", 3);
				{
					append.beginSizedList("1", 3);
					{
						append.item("1.1");
						append.beginSizedList("1.2", 2);
						{
							append.item("1.2.1");
							append.item("1.2.2");
						}
						append.endSizedList();
						append.item("1.3");
					}
					append.endSizedList();
					append.beginSizedList("2", 2);
					{
						append.beginSizedList("2.1", 0);
						append.endSizedList();
						append.beginSizedList("2.2", 2);
						{
							append.item("2.2.1");
							append.beginSizedList("2.2.2", 1);
							{
								append.item("2.2.2.1");
							}
							append.endSizedList();
						}
						append.endSizedList();
					}
					append.endSizedList();
					append.beginSizedList("3", 1);
					{
						append.item("3.1");
					}
					append.endSizedList();
					append.item("4");
				}
				append.endSizedList();
			}
		});
		assertEquals("Test of 3:\n"
						+ "\t1 of 3:\n"
						+ "\t\t\"1.1\"\n"
						+ "\t\t1.2 of 2:\n"
						+ "\t\t\t\"1.2.1\"\n"
						+ "\t\t\t\"1.2.2\"\n"
						+ "\t\t\"1.3\"\n"
						+ "\t2 of 2:\n"
						+ "\t\t2.1 of 0\n"
						+ "\t\t2.2 of 2:\n"
						+ "\t\t\t\"2.2.1\"\n"
						+ "\t\t\t2.2.2 of 1: \"2.2.2.1\"\n"
						+ "\t3 of 1: \"3.1\"\n"
						+ "\t\"4\""
				, toString.toString());
	}

	@Ignore("different types on same level should not be mixed")
	@Test public void testList_WithProperties() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, new Stringer<Object>() {
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.complexProperty("key1", 1);
				append.complexProperty("key2", 2f);
				append.beginSizedList("Test", 2);
				append.item("value1");
				append.item("value2");
				append.endSizedList();
				append.complexProperty("key3", 3);
				append.beginSizedList("Test", 2);
				append.item("value1");
				append.item("value2");
				append.endSizedList();
			}
		});
		assertEquals("(Object)"
						+ "key1=(Integer)1, key2=(Float)2.0\n"
						+ "Test of 2:\n"
						+ "\t\"value1\"\n"
						+ "\t\"value2\""
						+ "\n"
						+ "key3=(Integer)3\n"
						+ "Test of 2:\n"
						+ "\t\"value1\"\n"
						+ "\t\"value2\""
				, toString.toString());
	}

	@Test public void testList_Short() {
		ToStringer toString = new ToStringer(repo, PLACEHOLDER, false, new Stringer<Object>() {
			@Override public String getType(Object object) {
				return null;
			}
			@Override public void toString(@Nonnull ToStringAppender append, Object object) {
				append.beginSizedList("Test", 3);
				append.item("key1", "value1");
				append.item("key2", 2.0);
				append.item("key3", (byte)3);
				append.endSizedList();
			}
		});
		assertEquals("Test of 3#{"
						+ "key1->\"value1\""
						+ ", key2->(Double)2.0"
						+ ", key3->(Byte)3"
						+ "}"
				, toString.toString());
	}
}
