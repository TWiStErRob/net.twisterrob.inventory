package net.twisterrob.inventory.android.activity;

import org.junit.experimental.categories.Category;

import net.twisterrob.inventory.android.activity.data.PropertyEditActivity;
import net.twisterrob.inventory.android.content.DataBaseActor;
import net.twisterrob.inventory.android.test.actors.PropertyEditActivityActor;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@Category({On.Property.class, Op.CreatesBelonging.class})
public class PropertyEditActivityTest_Create extends EditActivityTest_Create<PropertyEditActivity> {
	public PropertyEditActivityTest_Create() {
		super(
				PropertyEditActivity.class,
				new PropertyEditActivityActor(),
				new BelongingValues(
						TEST_PROPERTY,
						TEST_PROPERTY_OTHER,
						TEST_PROPERTY_TYPE,
						TEST_PROPERTY_TYPE_OTHER,
						TEST_PROPERTY_TYPE_DEFAULT
				) {
					@Override protected DataBaseActor.BelongingAssertions createAssertions(
							DataBaseActor database) {
						return database.new PropertyAssertions();
					}
				});
	}

	@Override protected void createContainers() {
		// no container for property
	}
	@Override protected void createDuplicate() {
		database.createProperty(TEST_PROPERTY);
	}
}
