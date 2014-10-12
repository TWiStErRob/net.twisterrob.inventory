package net.twisterrob.java.web;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <ul>
 * <li>Primitive method call: <code>{@link Math#max(int, int) Math.max}(2, 4)</code>:<br>
 * <code>${call.static['java.lang.Math'].arg[2].arg[4].invoke['max']}</code>
 * <li>Collection as result: <code>container.lookup(film.edi, cinema.id).size()</code>:<br>
 * <code>${fn:length(call.init[container].arg[film.edi].arg[cinema.id].invoke['lookup']}</code>
 * <li>Explicit primitive args (required for anything other than int)<code>{@link Short#valueOf(short)}</code>:<br>
 * <code>${call.static['java.lang.Short'].shortArg[3].invoke['valueOf']}</code>
 * <li>Void method call, given {@link StringBuilder} {@code sb}: <code>sb.{@link StringBuilder#append(CharSequence) append}("hello")</code>:<br>
 * <code>${call.init[sb].type('java.lang.CharSequence').arg['hello'].invoke['append']}</code>
 * <li>
 * </ul>
 *
 * @author Based on How to call methods from EL expressions- pre JSP 2.0 trick for JSPs with JSTL
 * @see http://technology.amis.nl/2005/06/15/how-to-call-methods-from-el-expressions-pre-jsp-20-trick-for-jsps-with-jstl
 * @author papp.robert.s
 *
 */
public class InvokerMap implements Map<Object, Object> {
	private static enum Mode {
		Operation,
		Target,
		ParameterType,
		Parameter,
		Invoke
	}

	private Mode mode = Mode.Operation;

	private boolean isInstance;
	private Object target;
	private String methodName;
	private Class<?> nextArgType;
	private ArrayList<Object> args = new ArrayList<Object>();
	private ArrayList<Class<?>> argTypes = new ArrayList<Class<?>>();

	public Object get(Object key) {
		try {
			switch (mode) {
				case Target:
					target = key;
					mode = Mode.Operation;
					break;
				case Parameter:
					Object fixed = fixType(key);
					args.add(fixed);
					argTypes.add(getType(fixed));
					mode = Mode.Operation;
					break;
				case ParameterType:
					nextArgType = Class.forName(String.valueOf(key));
					mode = Mode.Operation;
					break;
				case Invoke:
					methodName = String.valueOf(key);
					Class<?> clazz = isInstance? target.getClass() : Class.forName((String)target);
					Method targetMethod = getMethod(clazz);
					if (targetMethod == null) {
						error("No such method, maybe need to pick overload by typing some arguments?", null);
						return null; // error should throw
					}
					targetMethod.setAccessible(true);
					mode = Mode.Operation;
					return targetMethod.invoke(target, args.toArray());
				case Operation:
				default:
					if ("init".equals(key)) {
						// if init is passed, reinitializing for the next invocation, next should be an object as "this"
						target = null;
						methodName = null;
						args.clear();
						argTypes.clear();
						isInstance = true;
						mode = Mode.Target;
					} else if ("static".equals(key)) {
						// if static is passed, reinitializing for the static invocation, next should be a FQCN
						target = null;
						methodName = null;
						args.clear();
						argTypes.clear();
						isInstance = false;
						mode = Mode.Target;
					} else if ("invoke".equals(key)) {
						// if invoke is passed, the next call to get will pass the name of the method to call
						mode = Mode.Invoke;
					} else if ("next".equals(key)) {
						// if next is passed, the next will tell us the FQCN of the arg coming up
						mode = Mode.ParameterType;
					} else if ("arg".equals(key)) {
						// if arg is passed, we can expect the next call to get to pass the value of the next attribute
						nextArgType = null;
						mode = Mode.Parameter;
					} else if ("intArg".equals(key)) { // int
						nextArgType = Integer.TYPE;
						mode = Mode.Parameter;
					} else if ("longArg".equals(key)) { // long
						nextArgType = Long.TYPE;
						mode = Mode.Parameter;
					} else if ("shortArg".equals(key)) { // short
						nextArgType = Short.TYPE;
						mode = Mode.Parameter;
					} else if ("doubleArg".equals(key)) { // double
						nextArgType = Double.TYPE;
						mode = Mode.Parameter;
					} else if ("floatArg".equals(key)) { // float
						nextArgType = Float.TYPE;
						mode = Mode.Parameter;
					} else if ("byteArg".equals(key)) { // byte
						nextArgType = Byte.TYPE;
						mode = Mode.Parameter;
					} else if ("charArg".equals(key)) { // char
						nextArgType = Character.TYPE;
						mode = Mode.Parameter;
					} else if ("booleanArg".equals(key)) { // boolean
						nextArgType = Boolean.TYPE;
						mode = Mode.Parameter;
					} else {
						error("I'm lost", null);
					}
					break;
			}
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			error("Something bad", ex);
		}
		return this;
	}

	protected Method getMethod(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		methods:
		for (Method method : methods) {
			Class<?>[] params = method.getParameterTypes();
			if (method.getName().equals(methodName) && params.length == args.size()) {
				int argNum = -1;
				for (Class<?> argType : argTypes) {
					argNum++;
					if (argType == null) {
						continue /*args*/;
					}
					if (!params[argNum].isAssignableFrom(argType)) {
						continue methods;
					}
				}
				return method;
			}
		}
		return null;
	}

	private Class<?> getType(Object arg) {
		if (nextArgType != null) {
			return nextArgType;
		} else if (arg != null) {
			return arg.getClass();
		} else {
			throw new IllegalArgumentException("Cannot determine type of argument #" + args.size()
					+ ", if you're using 'null' you must type if beforehand");
		}
	}

	private Object fixType(Object arg) {
		if (nextArgType == null) {
			return arg;
		} else if (nextArgType == Integer.TYPE) {
			return ((Number)arg).intValue();
		} else if (nextArgType == Long.TYPE) {
			return ((Number)arg).longValue();
		} else if (nextArgType == Short.TYPE) {
			return ((Number)arg).shortValue();
		} else if (nextArgType == Double.TYPE) {
			return ((Number)arg).doubleValue();
		} else if (nextArgType == Float.TYPE) {
			return ((Number)arg).floatValue();
		} else if (nextArgType == Byte.TYPE) {
			return ((Number)arg).byteValue();
		} else if (nextArgType == Character.TYPE) {
			return arg instanceof Number? (char)((Number)arg).intValue() : String.valueOf(arg).charAt(0);
		} else if (nextArgType == Boolean.TYPE) {
			return Boolean.valueOf(String.valueOf(arg));
		} else {
			return arg;
		}
	}

	private void error(String message, Throwable t) {
		StringBuilder call = new StringBuilder();
		call.append(String.valueOf(target));
		call.append('.');
		call.append(methodName);
		call.append('(');
		Iterator<Object> values = args.iterator();
		Iterator<Class<?>> types = argTypes.iterator();
		while (values.hasNext() && types.hasNext()) {
			Object arg = values.next();
			Class<?> type = types.next();
			call.append(type);
			call.append(": ");
			call.append(arg);
			if (values.hasNext()) {
				call.append(", ");
			}
		}
		call.append(')');
		if (values.hasNext() || types.hasNext()) {
			call.append("[construction invalid]");
		}
		throw new IllegalArgumentException(message + ": " + call.toString(), t);
	}

	// Mandatory implementations for Map<Object, Object>

	public int size() {
		return 0;
	}

	public boolean isEmpty() {
		return false;
	}

	public boolean containsKey(Object key) {
		return true;
	}

	public boolean containsValue(Object value) {
		return true;
	}

	public Object put(Object key, Object value) {
		return null;
	}

	public Object remove(Object key) {
		return null;
	}

	public void putAll(Map<?, ?> t) {
		// do nothing
	}

	public void clear() {
		// do nothing
	}

	public Set<Object> keySet() {
		return null;
	}

	public Collection<Object> values() {
		return null;
	}

	public Set<Map.Entry<Object, Object>> entrySet() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
