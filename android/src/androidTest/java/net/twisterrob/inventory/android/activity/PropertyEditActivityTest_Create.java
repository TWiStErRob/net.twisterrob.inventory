package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import android.support.test.espresso.NoActivityResumedException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.Stage;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.android.test.junit.TestWatcherStatus;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.PropertyEditActivity;
import net.twisterrob.inventory.android.content.DatabaseActor;
import net.twisterrob.inventory.android.test.*;
import net.twisterrob.inventory.android.test.actors.PropertyEditActivityActor;

import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
public class PropertyEditActivityTest_Create {
	@Rule public final ActivityTestRule<PropertyEditActivity> activity
			= new InventoryActivityRule<>(PropertyEditActivity.class);
	@Rule public final TestDatabaseRule db = new TestDatabaseRule();
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestName name = new TestName();
	@Rule public TestWatcherStatus status = new TestWatcherStatus();

	private final PropertyEditActivityActor propertyEditor = new PropertyEditActivityActor();
	private final DatabaseActor database = new DatabaseActor(db);

	@Before public void preconditions() {
		database.assertHasNoProperties();
	}

	@After public void lastOperationFinishesActivity() {
		if (status.isSucceeded()) {
			assertThat(activity.getActivity(), either(isFinishing()).or(isInStage(Stage.DESTROYED)));
		}
	}

	@Test public void testNameSaved() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.save();

		database.assertHasProperty(TEST_PROPERTY);
	}

	@Test public void testNameSavedAfterChange() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setName(TEST_PROPERTY_OTHER);
		propertyEditor.save();

		database.assertHasProperty(TEST_PROPERTY_OTHER);
	}

	@Test public void testCreateExisting() throws IOException {
		database.createProperty(TEST_PROPERTY);
		database.assertHasProperty(TEST_PROPERTY);

		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.save();

		propertyEditor.checkToastAlreadyExists();
		propertyEditor.assertIsOpen();
		database.assertHasProperty(TEST_PROPERTY);

		// clean up activity
		propertyEditor.tryClose();
		propertyEditor.confirmDirtyDialog();
	}

	@Test public void testDescriptionSaved() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setDescription(TEST_DESCRIPTION);

		propertyEditor.save();
		database.assertPropertyHasDescription(TEST_PROPERTY, TEST_DESCRIPTION);
	}

	@Test public void testDescriptionSavedAfterChange() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setDescription(TEST_DESCRIPTION);
		propertyEditor.setDescription(TEST_DESCRIPTION_OTHER);

		propertyEditor.save();
		database.assertPropertyHasDescription(TEST_PROPERTY, TEST_DESCRIPTION_OTHER);
	}

	@Test public void testTypeSaved() throws IOException {
		propertyEditor.checkType(R.string.property_other);
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setType(TEST_PROPERTY_TYPE);

		propertyEditor.save();
		database.assertPropertyHasType(TEST_PROPERTY, TEST_PROPERTY_TYPE);
	}

	@Test public void testTypeSavedAfterChange() throws IOException {
		propertyEditor.checkType(R.string.property_other);
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setType(TEST_PROPERTY_TYPE);
		propertyEditor.setType(TEST_PROPERTY_TYPE_OTHER);

		propertyEditor.save();
		database.assertPropertyHasType(TEST_PROPERTY, TEST_PROPERTY_TYPE_OTHER);
	}

	@Test public void testImageSaved() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, TEST_PROPERTY + "\n" + name.getMethodName());

		propertyEditor.save();
		database.assertPropertyHasImage(TEST_PROPERTY, TEST_IMAGE_COLOR);
	}

	@Test public void testImageSavedAfterChange() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, "first image\n" + name.getMethodName());
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR_OTHER, "second image\n" + name.getMethodName());

		propertyEditor.save();
		database.assertPropertyHasImage(TEST_PROPERTY, TEST_IMAGE_COLOR_OTHER);
	}

	@Test public void testRotate() throws IOException {
		fillInEverything();

		propertyEditor.rotate();
		checkEverythingFilledIn();
		database.assertHasNoProperty(TEST_PROPERTY);

		propertyEditor.rotate();
		checkEverythingFilledIn();
		database.assertHasNoProperty(TEST_PROPERTY);

		propertyEditor.save();
		checkEverythingSaved();
	}

	@Test(expected = NoActivityResumedException.class)
	public void testDirtyInitiallyClean() {
		propertyEditor.tryClose();
		DialogMatchers.assertNoDialogIsDisplayed();
	}

	@Test public void testDirtyCanSave() throws IOException {
		fillInEverything();
		propertyEditor.tryClose();
		propertyEditor.saveFromDirtyDialog();
		checkEverythingSaved();
	}

	@Test public void testNameChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() {
				propertyEditor.setName(TEST_PROPERTY);
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.checkName(TEST_PROPERTY);
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.setName(TEST_PROPERTY_OTHER);
			}
		});
	}

	@Test public void testDescriptionChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() {
				propertyEditor.setDescription(TEST_DESCRIPTION);
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.checkDescription(TEST_DESCRIPTION);
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.setDescription(TEST_DESCRIPTION_OTHER);
			}
		});
	}

	@Test public void testTypeChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() {
				propertyEditor.setType(TEST_PROPERTY_TYPE);
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.checkType(TEST_PROPERTY_TYPE);
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.setType(TEST_PROPERTY_TYPE_OTHER);
			}
		});
	}

	@Test public void testImageChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() throws IOException {
				propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR,
						"first image\n" + name.getMethodName());
			}
		}, new DirtyAction() {
			public void execute() {
				propertyEditor.checkPicture(TEST_IMAGE_COLOR);
			}
		}, new DirtyAction() {
			public void execute() throws IOException {
				propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR_OTHER,
						"second image\n" + name.getMethodName());
			}
		});
	}

	private void testDirty(DirtyAction doFirstEdit, DirtyAction checkFirstEditPreserved, DirtyAction doSecondEdit)
			throws Throwable {
		// triggers dirty
		doFirstEdit.execute();
		propertyEditor.tryClose();
		propertyEditor.cancelDirtyDialog();
		database.assertHasNoProperties();
		checkFirstEditPreserved.execute();
		// still dirty after cancel
		propertyEditor.tryClose();
		propertyEditor.cancelDirtyDialog();
		database.assertHasNoProperties();
		checkFirstEditPreserved.execute();
		// new change still leaves it dirty
		doSecondEdit.execute();
		propertyEditor.tryClose();
		propertyEditor.confirmDirtyDialog();
		// data wasn't saved
		database.assertHasNoProperties();
	}

	private void fillInEverything() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setDescription(TEST_DESCRIPTION);
		propertyEditor.setType(TEST_PROPERTY_TYPE);
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, name.getMethodName());
	}
	private void checkEverythingFilledIn() {
		propertyEditor.checkName(TEST_PROPERTY);
		propertyEditor.checkDescription(TEST_DESCRIPTION);
		propertyEditor.checkType(TEST_PROPERTY_TYPE);
		propertyEditor.checkPicture(TEST_IMAGE_COLOR);
	}
	private void checkEverythingSaved() {
		database.assertHasProperty(TEST_PROPERTY);
		database.assertPropertyHasDescription(TEST_PROPERTY, TEST_DESCRIPTION);
		database.assertPropertyHasType(TEST_PROPERTY, TEST_PROPERTY_TYPE);
		database.assertPropertyHasImage(TEST_PROPERTY, TEST_IMAGE_COLOR);
	}

	interface DirtyAction {
		void execute() throws Throwable;
	}
}
