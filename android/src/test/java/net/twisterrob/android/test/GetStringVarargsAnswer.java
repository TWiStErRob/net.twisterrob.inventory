package net.twisterrob.android.test;

import java.lang.reflect.Field;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.support.annotation.*;

import edu.emory.mathcs.backport.java.util.Arrays;

import net.twisterrob.java.utils.ReflectionTools;

/**
 * <pre><code>
 * when(res.getString(anyInt(), anyVararg())).thenAnswer(new GetStringVarargsAnswer(R.string.class));
 * when(context.getString(anyInt(), anyVararg())).thenAnswer(new GetStringVarargsAnswer(R.string.class));
 * </code></pre>
 */
public class GetStringVarargsAnswer implements Answer<String> {
	private final Class<?>[] resClasses;

	public GetStringVarargsAnswer(@NonNull Class<?>... resClasses) {
		this.resClasses = resClasses;
	}

	@Override public String answer(InvocationOnMock invocation) throws Throwable {
		StringBuilder sb = new StringBuilder();
		for (Object arg : invocation.getArguments()) {
			if (sb.length() == 0) {
				Field stringId = findId((Integer)arg);
				sb.append("R.string.").append(stringId.getName()).append(" with ");
			} else {
				sb.append('<').append(arg).append('>').append(", ");
			}
		}
		if (invocation.getArguments().length > 1) {
			sb.delete(sb.length() - 2, sb.length());
		} else {
			sb.append("no arguments");
		}
		return sb.toString();
	}

	private Field findId(@StringRes int id) throws NoSuchFieldException {
		for (Class<?> clazz : resClasses) {
			Field field = ReflectionTools.tryFindConstant(clazz, id);
			if (field != null) {
				return field;
			}
		}
		throw new NoSuchFieldException("Cannot find " + id + " in any of " + Arrays.toString(resClasses));
	}
}
