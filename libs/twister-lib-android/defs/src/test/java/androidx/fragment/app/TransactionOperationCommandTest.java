package androidx.fragment.app;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TransactionOperationCommandTest {

	@Parameters(name = "opToString#{index}({0,number,#})={1}")
	public static List<Object[]> data() {
		List<Object[]> params = new ArrayList<>();
		for (Field field : FragmentTransaction.class.getDeclaredFields()) {
			String FIELD_PREFIX = "OP_";
			if (field.getName().startsWith(FIELD_PREFIX)) {
				try {
					String expected = field.getName().substring(FIELD_PREFIX.length());
					int fieldValue = (int)field.get(null);
					params.add(new Object[] {fieldValue, expected});
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		params.add(new Object[] {-1, "cmd::-1"});
		params.add(new Object[] {42, "cmd::42"});
		return params;
	}

	private final int input;
	private final String expected;

	public TransactionOperationCommandTest(int input, String expected) {
		this.input = input;
		this.expected = expected;
	}

	@Test public void test() {
		String op = TransactionOperationCommand.Converter.toString(input);
		assertEquals(expected, op);
	}
}
