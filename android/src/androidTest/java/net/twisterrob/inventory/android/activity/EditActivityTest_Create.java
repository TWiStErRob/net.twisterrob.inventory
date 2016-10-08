package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.Stage;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.android.test.junit.TestWatcherStatus;
import net.twisterrob.inventory.android.content.DataBaseActor;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.EditActivityActor;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.UseCase.Error;
import net.twisterrob.inventory.android.test.suites.QuickSuite;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
public abstract class EditActivityTest_Create<T extends Activity> {
	@Rule public final InventoryActivityRule<T> activity;
	@Rule public final DataBaseActor database = new DataBaseActor();
	private final DataBaseActor.BelongingAssertions db;
	private final BelongingValues belonging;
	@Rule public TemporaryFolder temp = new TemporaryFolder();
	@Rule public TestName testName = new TestName();
	@Rule public TestWatcherStatus status = new TestWatcherStatus();

	private final EditActivityActor editor;
	public EditActivityTest_Create(Class<T> activityClass, EditActivityActor editorActor, BelongingValues belonging) {
		editor = editorActor;
		activity = new InventoryActivityRule<T>(activityClass) {
			@Override protected void setDefaults() {
				super.setDefaults();
				createContainers();
			}
		};
		this.belonging = belonging;
		this.db = belonging.createAssertions(database);
	}

	protected abstract void createContainers();
	protected abstract void createDuplicate();

	@Before public void preconditions() {
		db.assertHasNoBelongingOfType();
	}

	@After public void lastOperationFinishesActivity() {
		if (status.isSucceeded()) {
			assertThat(activity.getActivity(), either(isFinishing()).or(isInStage(Stage.DESTROYED)));
		}
	}

	@Test public void testNameSaved() {
		editor.setName(belonging.getName());
		editor.save();

		db.assertHasBelonging(belonging.getName());
	}

	@Test public void testNameSavedAfterChange() {
		editor.setName(belonging.getName());
		editor.setName(belonging.getOtherName());
		editor.save();

		db.assertHasBelonging(belonging.getOtherName());
	}

	@Category({Error.class, Op.ChecksMessage.class})
	@Test public void testCreateExisting() {
		createDuplicate();
		db.assertHasBelonging(belonging.getName());

		onView(isRoot()).perform(waitForToastsToDisappear());
		editor.setName(belonging.getName());
		editor.save().checkToastAlreadyExists();

		editor.assertIsInFront();
		db.assertHasBelonging(belonging.getName());

		// clean up activity
		editor.close();
		editor.confirmDirtyDialog();
	}

	@Test public void testDescriptionSaved() {
		editor.setName(belonging.getName());
		editor.setDescription(TEST_DESCRIPTION);

		editor.save();
		db.assertHasDescription(belonging.getName(), TEST_DESCRIPTION);
	}

	@Test public void testDescriptionSavedAfterChange() {
		editor.setName(belonging.getName());
		editor.setDescription(TEST_DESCRIPTION);
		editor.setDescription(TEST_DESCRIPTION_OTHER);

		editor.save();
		db.assertHasDescription(belonging.getName(), TEST_DESCRIPTION_OTHER);
	}

	@Category({QuickSuite.QuickCategory.class})
	@Test public void testTypeSaved() {
		editor.checkType(belonging.getDefaultType());
		editor.setName(belonging.getName());
		editor.setType(belonging.getType());

		editor.save();
		db.assertHasType(belonging.getName(), belonging.getType());
	}

	@Category({QuickSuite.QuickCategory.class})
	@Test public void testTypeSavedAfterChange() {
		editor.checkType(belonging.getDefaultType());
		editor.setName(belonging.getName());
		editor.setType(belonging.getType());
		editor.setType(belonging.getOtherType());

		editor.save();
		db.assertHasType(belonging.getName(), belonging.getOtherType());
	}

	@Test public void testImageSaved() throws IOException {
		editor.setName(belonging.getName());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, belonging.getName() + "\n" + testName.getMethodName());

		editor.save();
		db.assertHasImage(belonging.getName(), TEST_IMAGE_COLOR);
	}

	@Test public void testImageSavedAfterChange() throws IOException {
		editor.setName(belonging.getName());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, "first image\n" + testName.getMethodName());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR_OTHER, "second image\n" + testName.getMethodName());

		editor.save();
		db.assertHasImage(belonging.getName(), TEST_IMAGE_COLOR_OTHER);
	}

	@Category({Op.Rotates.class, QuickSuite.QuickCategory.class})
	@Test public void testRotate() throws IOException {
		fillInEverything();

		editor.rotate();
		checkEverythingFilledIn();
		db.assertHasNoBelonging(belonging.getName());

		editor.rotate();
		checkEverythingFilledIn();
		db.assertHasNoBelonging(belonging.getName());

		editor.save();
		checkEverythingSaved();
	}

	@Category({UseCase.InitialCondition.class})
	@Test(expected = NoActivityResumedException.class)
	public void testDirtyInitiallyClean() {
		editor.close();
		DialogMatchers.assertNoDialogIsDisplayed();
	}

	@Category({QuickSuite.QuickCategory.class})
	@Test public void testDirtyCanSave() throws IOException {
		fillInEverything();
		editor.close();
		editor.saveFromDirtyDialog();
		checkEverythingSaved();
	}

	@Test public void testNameChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() {
				editor.setName(belonging.getName());
			}
		}, new DirtyAction() {
			public void execute() {
				editor.checkName(belonging.getName());
			}
		}, new DirtyAction() {
			public void execute() {
				editor.setName(belonging.getOtherName());
			}
		});
	}

	@Test public void testDescriptionChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() {
				editor.setDescription(TEST_DESCRIPTION);
			}
		}, new DirtyAction() {
			public void execute() {
				editor.checkDescription(TEST_DESCRIPTION);
			}
		}, new DirtyAction() {
			public void execute() {
				editor.setDescription(TEST_DESCRIPTION_OTHER);
			}
		});
	}

	@Test public void testTypeChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() {
				editor.setType(belonging.getType());
			}
		}, new DirtyAction() {
			public void execute() {
				editor.checkType(belonging.getType());
			}
		}, new DirtyAction() {
			public void execute() {
				editor.setType(belonging.getOtherType());
			}
		});
	}

	@Test public void testImageChangeTriggersDirty() throws Throwable {
		testDirty(new DirtyAction() {
			public void execute() throws IOException {
				editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR,
						"first image\n" + testName.getMethodName());
			}
		}, new DirtyAction() {
			public void execute() {
				editor.checkPicture(TEST_IMAGE_COLOR);
			}
		}, new DirtyAction() {
			public void execute() throws IOException {
				editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR_OTHER,
						"second image\n" + testName.getMethodName());
			}
		});
	}

	protected void testDirty(DirtyAction doFirstEdit, DirtyAction checkFirstEditPreserved, DirtyAction doSecondEdit)
			throws IOException {
		// triggers dirty
		doFirstEdit.execute();
		editor.close();
		editor.cancelDirtyDialog();
		preconditions();
		checkFirstEditPreserved.execute();
		// still dirty after cancel
		editor.close();
		editor.cancelDirtyDialog();
		preconditions();
		checkFirstEditPreserved.execute();
		// new change still leaves it dirty
		doSecondEdit.execute();
		editor.close();
		editor.confirmDirtyDialog();
		// data wasn't saved
		preconditions();
	}

	protected void fillInEverything() throws IOException {
		editor.setName(belonging.getName());
		editor.setDescription(TEST_DESCRIPTION);
		editor.setType(belonging.getType());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, testName.getMethodName());
	}
	protected void checkEverythingFilledIn() {
		editor.checkName(belonging.getName());
		editor.checkDescription(TEST_DESCRIPTION);
		editor.checkType(belonging.getType());
		editor.checkPicture(TEST_IMAGE_COLOR);
	}
	protected void checkEverythingSaved() {
		db.assertHasBelonging(belonging.getName());
		db.assertHasDescription(belonging.getName(), TEST_DESCRIPTION);
		db.assertHasType(belonging.getName(), belonging.getType());
		db.assertHasImage(belonging.getName(), TEST_IMAGE_COLOR);
	}

	interface DirtyAction {
		void execute() throws IOException;
	}

	protected static abstract class BelongingValues {
		private final String name;
		private final String otherName;
		private final @StringRes int type;
		private final @StringRes int otherType;
		private final @StringRes int defaultType;

		public BelongingValues(String name, String otherName,
				@StringRes int type, @StringRes int otherType, @StringRes int defaultType) {
			this.name = name;
			this.otherName = otherName;
			this.type = type;
			this.otherType = otherType;
			this.defaultType = defaultType;
		}
		public String getName() {
			return name;
		}
		public String getOtherName() {
			return otherName;
		}
		public int getType() {
			return type;
		}
		public int getOtherType() {
			return otherType;
		}
		public int getDefaultType() {
			return defaultType;
		}

		protected abstract DataBaseActor.BelongingAssertions createAssertions(DataBaseActor database);
	}
}
