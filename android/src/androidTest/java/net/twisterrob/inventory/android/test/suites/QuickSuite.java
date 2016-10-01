package net.twisterrob.inventory.android.test.suites;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
		AllTestsSuite.class,
})
@Categories.IncludeCategory(matchAny = true, value = {

})
@Categories.ExcludeCategory(matchAny = true, value = {

})
@SuppressWarnings("DefaultAnnotationParam")
public class QuickSuite {
}
