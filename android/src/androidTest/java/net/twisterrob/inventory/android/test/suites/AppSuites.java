package net.twisterrob.inventory.android.test.suites;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import net.twisterrob.inventory.android.test.categories.*;

public class AppSuites {

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.Main.class,
	})
	public static class MainSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.Import.class,
			On.Export.class,
	})
	public static class BackupSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			Op.ChecksMessage.class,
	})
	public static class LocalizationSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			Op.Cancels.class,
			Op.Rotates.class,
			UseCase.Error.class,
	})
	public static class EdgeCasesSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			UseCase.Complex.class,
	})
	public static class SmokeSuite {
	}
}
