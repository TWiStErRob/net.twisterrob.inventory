package net.twisterrob.inventory.android.test.suites;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.twisterrob.inventory.android.test.categories.On;

@RunWith(Suite.class)
@SuiteClasses({
		ModelSuites.PropertySuite.class,
		ModelSuites.RoomSuite.class,
		ModelSuites.ItemSuite.class,
		ModelSuites.CategorySuite.class,
		ModelSuites.ListSuite.class,
})
public class ModelSuites {

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.Property.class,
	})
	public static class PropertySuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.Room.class,
	})
	public static class RoomSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.Item.class,
	})
	public static class ItemSuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.Category.class,
	})
	public static class CategorySuite {
	}

	@RunWith(Categories.class)
	@SuiteClasses({AllTestsSuite.class})
	@IncludeCategory({
			On.List.class,
	})
	public static class ListSuite {
	}
}
