package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import android.support.test.espresso.NoActivityResumedException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.Stage;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.android.test.junit.TestWatcherStatus;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.PropertyEditActivity;
import net.twisterrob.inventory.android.content.DataBaseActor;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.PropertyEditActivityActor;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.UseCase.Error;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Property.class, Op.CreatesBelonging.class})
public class PropertyEditActivityTest_Create {
	@Rule public final ActivityTestRule<PropertyEditActivity> activity
			= new InventoryActivityRule<>(PropertyEditActivity.class);
	@Rule public final DataBaseActor db = new DataBaseActor();
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestName name = new TestName();
	@Rule public TestWatcherStatus status = new TestWatcherStatus();

	private final PropertyEditActivityActor propertyEditor = new PropertyEditActivityActor();

	@Before public void preconditionsForCreatingAProperty() {
		db.assertHasNoProperties();
	}

	@After public void lastOperationFinishesActivity() {
		if (status.isSucceeded()) {
			assertThat(activity.getActivity(), either(isFinishing()).or(isInStage(Stage.DESTROYED)));
		}
	}

	@Test public void testNameSaved() {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.save();

		db.assertHasProperty(TEST_PROPERTY);
	}

	@Test public void testNameSavedAfterChange() {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setName(TEST_PROPERTY_OTHER);
		propertyEditor.save();

		db.assertHasProperty(TEST_PROPERTY_OTHER);
	}

	@Category({Error.class, Op.ChecksMessage.class})
	@Test public void testCreateExisting() {
		db.createProperty(TEST_PROPERTY);
		db.assertHasProperty(TEST_PROPERTY);

		onView(isRoot()).perform(waitForToastsToDisappear());
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.save().checkToastAlreadyExists();

		propertyEditor.assertIsOpen();
		db.assertHasProperty(TEST_PROPERTY);

		// clean up activity
		propertyEditor.close();
		propertyEditor.confirmDirtyDialog();
	}

	@Test public void testDescriptionSaved() {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setDescription(TEST_DESCRIPTION);

		propertyEditor.save();
		db.assertPropertyHasDescription(TEST_PROPERTY, TEST_DESCRIPTION);
	}

	@Test public void testDescriptionSavedAfterChange() {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setDescription(TEST_DESCRIPTION);
		propertyEditor.setDescription(TEST_DESCRIPTION_OTHER);

		propertyEditor.save();
		db.assertPropertyHasDescription(TEST_PROPERTY, TEST_DESCRIPTION_OTHER);
	}

	@Test public void testTypeSaved() {
		propertyEditor.checkType(R.string.property_other);
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setType(TEST_PROPERTY_TYPE);

		propertyEditor.save();
		db.assertPropertyHasType(TEST_PROPERTY, TEST_PROPERTY_TYPE);
	}

	@Test public void testTypeSavedAfterChange() {
		propertyEditor.checkType(R.string.property_other);
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.setType(TEST_PROPERTY_TYPE);
		propertyEditor.setType(TEST_PROPERTY_TYPE_OTHER);

		propertyEditor.save();
		db.assertPropertyHasType(TEST_PROPERTY, TEST_PROPERTY_TYPE_OTHER);
	}

	@Test public void testImageSaved() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, TEST_PROPERTY + "\n" + name.getMethodName());

		propertyEditor.save();
		db.assertPropertyHasImage(TEST_PROPERTY, TEST_IMAGE_COLOR);
	}

	@Test public void testImageSavedAfterChange() throws IOException {
		propertyEditor.setName(TEST_PROPERTY);
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, "first image\n" + name.getMethodName());
		propertyEditor.takePicture(temp.newFile(), TEST_IMAGE_COLOR_OTHER, "second image\n" + name.getMethodName());

		propertyEditor.save();
		db.assertPropertyHasImage(TEST_PROPERTY, TEST_IMAGE_COLOR_OTHER);
	}

	@Category({Op.Rotates.class})
	@Test public void testRotate() throws IOException {
		fillInEverything();

		propertyEditor.rotate();
		checkEverythingFilledIn();
		db.assertHasNoProperty(TEST_PROPERTY);

		propertyEditor.rotate();
		checkEverythingFilledIn();
		db.assertHasNoProperty(TEST_PROPERTY);

		propertyEditor.save();
		checkEverythingSaved();
	}

	@Category({UseCase.InitialCondition.class})
	@Test(expected = NoActivityResumedException.class)
	public void testDirtyInitiallyClean() {
		propertyEditor.close();
		DialogMatchers.assertNoDialogIsDisplayed();
	}

	@Test public void testDirtyCanSave() throws IOException {
		fillInEverything();
		propertyEditor.close();
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
			throws IOException {
		// triggers dirty
		doFirstEdit.execute();
		propertyEditor.close();
		propertyEditor.cancelDirtyDialog();
		db.assertHasNoProperties();
		checkFirstEditPreserved.execute();
		// still dirty after cancel
		propertyEditor.close();
		propertyEditor.cancelDirtyDialog();
		db.assertHasNoProperties();
		checkFirstEditPreserved.execute();
		// new change still leaves it dirty
		doSecondEdit.execute();
		propertyEditor.close();
		propertyEditor.confirmDirtyDialog();
		// data wasn't saved
		db.assertHasNoProperties();
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
		db.assertHasProperty(TEST_PROPERTY);
		db.assertPropertyHasDescription(TEST_PROPERTY, TEST_DESCRIPTION);
		db.assertPropertyHasType(TEST_PROPERTY, TEST_PROPERTY_TYPE);
		db.assertPropertyHasImage(TEST_PROPERTY, TEST_IMAGE_COLOR);
	}

	interface DirtyAction {
		void execute() throws IOException;
	}
}
