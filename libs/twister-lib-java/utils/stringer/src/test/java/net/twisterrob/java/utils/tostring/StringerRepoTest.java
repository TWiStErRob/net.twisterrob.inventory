package net.twisterrob.java.utils.tostring;

import org.hamcrest.Matcher;
import org.junit.*;
import org.mockito.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import net.twisterrob.java.utils.tostring.stringers.*;

public class StringerRepoTest {
	private StringerRepo repo;
	@Before public void setup() {
		MockitoAnnotations.initMocks(this);
		repo = new StringerRepo();
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerNonExistent() {
		repo.register("non.existent.Class", stringer());
	}

	@SuppressWarnings("ConstantConditions")
	@Test(expected = IllegalArgumentException.class)
	public void registerNull() {
		repo.register("java.lang.Object", null);
	}

	@Test public void registerValid() {
		repo.register(DirectFromObject.class, StringerRepoTest.<DirectFromObject>stringer());
		//noinspection RedundantTypeArguments let's be explicit
		repo.register(DirectFromObject.class, StringerRepoTest.<Object>stringer());

		repo.register(GrandChild.class, StringerRepoTest.<GrandChild>stringer());
		repo.register(GrandChild.class, StringerRepoTest.<Child>stringer());
		repo.register(GrandChild.class, StringerRepoTest.<Super>stringer());

		repo.register(ChildSuperInterfaces.class, StringerRepoTest.<ChildSuperInterfaces>stringer());
		repo.register(ChildSuperInterfaces.class, StringerRepoTest.<SuperInterfaces>stringer());
		repo.register(ChildSuperInterfaces.class, StringerRepoTest.<SuperInterface1>stringer());
		repo.register(ChildSuperInterfaces.class, StringerRepoTest.<SuperInterface2>stringer());

		repo.register(ChildInterfaces.class, StringerRepoTest.<ChildInterfaces>stringer());
		repo.register(ChildInterfaces.class, StringerRepoTest.<ChildInterfacesSuper>stringer());
		repo.register(ChildInterfaces.class, StringerRepoTest.<ChildInterface1>stringer());
		repo.register(ChildInterfaces.class, StringerRepoTest.<ChildInterface2>stringer());
	}

	@Test public void findNullType() {
		assertThat(repo.find(null), instanceOf(NullTypeStringer.class));
	}
	@Test public void findNullValue() {
		assertThat(repo.findByValue(null), instanceOf(NullStringer.class));
	}
	@Test public void findUnregisteredValue() {
		assertThat(repo.findByValue(new Object()), sameInstance(repo.getDefault()));
	}
	@Test public void findRegisteredValue() {
		Stringer<DirectFromObject> stringer = stringer();

		repo.register(DirectFromObject.class, stringer);

		assertThat(repo.findByValue(new DirectFromObject()), sameInstance(stringer));
	}

	@Test public void findNonRegistered() {
		@SuppressWarnings("rawtypes") Matcher<Stringer> returnsDefaultStringer = sameInstance(repo.getDefault());
		assertThat(repo.find(DirectFromObject.class), returnsDefaultStringer);
		assertThat(repo.find(Object.class), returnsDefaultStringer);
		assertThat(repo.find(Cloneable.class), returnsDefaultStringer);
		assertThat(repo.find(int.class), returnsDefaultStringer);
	}

	@Test public void findSimple() {
		Stringer<DirectFromObject> stringer = stringer();

		repo.register(DirectFromObject.class, stringer);

		assertSame(stringer, repo.find(DirectFromObject.class));
	}

	@Test public void findsInherited() {
		Stringer<Child> stringer = stringer();

		repo.register(Child.class, stringer);

		assertSame(stringer, repo.find(Child.class));
	}

	@Test public void findsSuper() {
		Stringer<Super> stringer = stringer();

		repo.register(Super.class, stringer);

		assertSame(stringer, repo.find(Child.class));
		assertSame(stringer, repo.find(GrandChild.class));
	}

	@Test public void findsClosestClass() {
		Stringer<Super> superStringer = stringer("super");
		Stringer<Child> childStringer = stringer("child");
		Stringer<GrandChild> grandChildStringer = stringer("grandchild");

		repo.register(Super.class, superStringer);
		repo.register(Child.class, childStringer);
		repo.register(GrandChild.class, grandChildStringer);

		assertSame(grandChildStringer, repo.find(GrandChild.class));
		assertSame(childStringer, repo.find(Child.class));
		assertSame(superStringer, repo.find(Super.class));
	}

	@SuppressWarnings("rawtypes")
	@Test public void findsInterfaceOnChild() {
		Stringer<ChildInterface1> ifaceStringer1 = stringer("child1");
		Stringer<ChildInterface2> ifaceStringer2 = stringer("child2");

		repo.register(ChildInterface1.class, ifaceStringer1);
		repo.register(ChildInterface2.class, ifaceStringer2);

		assertThat(repo.find(ChildInterfaces.class),
				either(sameInstance(ifaceStringer1)).or(sameInstance(ifaceStringer2)));
	}

	@SuppressWarnings("rawtypes")
	@Test public void findsInterfaceOnSuper() {
		Stringer<SuperInterface1> ifaceStringer1 = stringer("super1");
		Stringer<SuperInterface2> ifaceStringer2 = stringer("super2");

		repo.register(SuperInterface1.class, ifaceStringer1);
		repo.register(SuperInterface2.class, ifaceStringer2);

		assertThat(repo.find(ChildSuperInterfaces.class),
				either(sameInstance(ifaceStringer1)).or(sameInstance(ifaceStringer2)));
	}

	@SuppressWarnings("rawtypes")
	@Test public void findsExtendedInterface() {
		Stringer<BaseInterface> stringer = stringer();

		repo.register(BaseInterface.class, stringer);

		assertThat(repo.find(ExtendedInterfaceImpl.class), sameInstance(stringer));
	}

	// @formatter:off
	class DirectFromObject {}

	class Super {}
	class Child extends Super {}
	class GrandChild extends Child {}

	interface SuperInterface1 {}
	interface SuperInterface2 {}
	class SuperInterfaces implements SuperInterface1, SuperInterface2 {}
	class ChildSuperInterfaces extends SuperInterfaces {}

	interface ChildInterface1 {}
	interface ChildInterface2 {}
	class ChildInterfacesSuper {}
	class ChildInterfaces extends ChildInterfacesSuper implements ChildInterface1, ChildInterface2 {}

	interface BaseInterface {}
	interface ExtendedInterface extends BaseInterface {}
	class ExtendedInterfaceImpl implements ExtendedInterface {}
	// @formatter:on

	@SuppressWarnings("unchecked")
	private static <T> Stringer<T> stringer() {
		return Mockito.mock(Stringer.class);
	}
	@SuppressWarnings("unchecked")
	private static <T> Stringer<T> stringer(String name) {
		return Mockito.mock(Stringer.class, name);
	}
	@SuppressWarnings("rawtypes")
	private static Matcher<Stringer> sameInstance(Stringer<?> stringer) {
		return org.hamcrest.Matchers.sameInstance((Stringer)stringer);
	}
}
