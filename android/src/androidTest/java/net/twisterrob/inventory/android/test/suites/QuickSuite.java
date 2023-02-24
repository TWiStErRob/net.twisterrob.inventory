package net.twisterrob.inventory.android.test.suites;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
		AllTestsSuite.class,
})
@Categories.IncludeCategory(matchAny = true, value = {
		// add @Category({QuickSuite.QuickCategory.class}) to tests of interest
//		QuickSuite.QuickCategory.class,
})
@Categories.ExcludeCategory(matchAny = true, value = {

})
@SuppressWarnings({"DefaultAnnotationParam", "deprecation"})
public class QuickSuite {
	/** @deprecated don't commit any uses of this class */
	@Deprecated public interface QuickCategory {
	}
}
