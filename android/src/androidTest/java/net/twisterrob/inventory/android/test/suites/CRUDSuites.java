package net.twisterrob.inventory.android.test.suites;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.twisterrob.inventory.android.test.categories.Op;

@RunWith(Suite.class)
@SuiteClasses({
		CRUDSuites.CreateSuite.class,
		CRUDSuites.EditSuite.class,
		CRUDSuites.MoveSuite.class,
		CRUDSuites.DeleteSuite.class,
})
public class CRUDSuites {

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			Op.CreatesBelonging.class,
	})
	public static class CreateSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			Op.EditsBelonging.class,
	})
	public static class EditSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			Op.MovesBelonging.class,
	})
	public static class MoveSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			Op.DeletesBelonging.class,
	})
	public static class DeleteSuite {
	}
}
