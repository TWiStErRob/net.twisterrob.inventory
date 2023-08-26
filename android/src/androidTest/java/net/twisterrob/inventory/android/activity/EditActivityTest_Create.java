package net.twisterrob.inventory.android.activity;

import java.io.IOException;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.*;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssume.assumeThat;

import android.app.Activity;

import androidx.annotation.StringRes;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.Stage;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.DialogMatchers;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.ChangeTypeDialogActor;
import net.twisterrob.inventory.android.test.actors.EditActivityActor;
import net.twisterrob.inventory.android.test.actors.KeywordsDialogActor;
import net.twisterrob.inventory.android.test.categories.*;
import net.twisterrob.inventory.android.test.categories.UseCase.Error;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;
import static net.twisterrob.inventory.android.content.Constants.*;

// TODO tests containing setType may fail for Item when run with animations enabled and running in landscape
@RunWith(AndroidJUnit4.class)
@Category(Op.CreatesBelonging.class)
public abstract class EditActivityTest_Create<T extends Activity> {
	@Rule public final InventoryActivityRule<T> activity;
	@Rule public final DataBaseActor database = new AppSingletonDatabaseActor();
	private final DataBaseActor.BelongingAssertions db;
	private final BelongingValues belonging;
	@Rule public final TemporaryFolder temp = new TemporaryFolder();
	@Rule public final TestName testName = new TestName();

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

	// @After, but only when successful, so need to manually call it
	public void lastOperationFinishesActivity() {
		assertThat(activity.getActivity(), either(isFinishing()).or(isInStage(Stage.DESTROYED)));
	}

	@Test public void testNameSaved() {
		editor.setName(belonging.getName());
		editor.save();

		db.assertHasBelonging(belonging.getName());
		lastOperationFinishesActivity();
	}

	@Test public void testNameSavedAfterChange() {
		editor.setName(belonging.getName());
		editor.setName(belonging.getOtherName());
		editor.save();

		db.assertHasBelonging(belonging.getOtherName());
		lastOperationFinishesActivity();
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
		lastOperationFinishesActivity();
	}

	@Test public void testDescriptionSaved() {
		editor.setName(belonging.getName());
		editor.setDescription(TEST_DESCRIPTION);

		editor.save();
		db.assertHasDescription(belonging.getName(), TEST_DESCRIPTION);
		lastOperationFinishesActivity();
	}

	@Test public void testDescriptionSavedAfterChange() {
		editor.setName(belonging.getName());
		editor.setDescription(TEST_DESCRIPTION);
		editor.setDescription(TEST_DESCRIPTION_OTHER);

		editor.save();
		db.assertHasDescription(belonging.getName(), TEST_DESCRIPTION_OTHER);
		lastOperationFinishesActivity();
	}

	@Test public void testTypeSaved() {
		editor.checkType(belonging.getDefaultType());
		editor.setName(belonging.getName());
		editor.setType(belonging.getType());

		editor.save();
		db.assertHasType(belonging.getName(), belonging.getType());
		lastOperationFinishesActivity();
	}

	@Test public void testTypeSavedAfterChange() {
		editor.checkType(belonging.getDefaultType());
		editor.setName(belonging.getName());
		editor.setType(belonging.getType());
		editor.setType(belonging.getOtherType());

		editor.save();
		db.assertHasType(belonging.getName(), belonging.getOtherType());
		lastOperationFinishesActivity();
	}

	@Test public void testImageSaved() throws IOException {
		editor.setName(belonging.getName());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, belonging.getName() + "\n" + testName.getMethodName());

		editor.save();
		db.assertHasImage(belonging.getName(), TEST_IMAGE_COLOR);
		lastOperationFinishesActivity();
	}

	@Test public void testImageSavedAfterChange() throws IOException {
		editor.setName(belonging.getName());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR, "first image\n" + testName.getMethodName());
		editor.takePicture(temp.newFile(), TEST_IMAGE_COLOR_OTHER, "second image\n" + testName.getMethodName());

		editor.save();
		db.assertHasImage(belonging.getName(), TEST_IMAGE_COLOR_OTHER);
		lastOperationFinishesActivity();
	}

	@Category({Op.Rotates.class})
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
		lastOperationFinishesActivity();
	}

	@Ignore
	@Test public void testChangeCategoryAndKeywordsDialog() {
		assumeThat(
				"Only items have change type button for now.",
				this,
				not(anyOf(
						instanceOf(PropertyEditActivityTest_Create.class),
						instanceOf(RoomEditActivityTest_Create.class)
				))
		);
		ChangeTypeDialogActor dialog = editor.changeType();

		KeywordsDialogActor keywords = dialog.showKeywords(belonging.getOtherType());
		keywords.assertKeywords(belonging.getOtherKeywords());
		keywords.close();

		dialog.cancel();
	}

	@Category({UseCase.InitialCondition.class})
	@Test(expected = NoActivityResumedException.class)
	public void testDirtyInitiallyClean() {
		editor.close();
		DialogMatchers.assertNoDialogIsDisplayed();
		lastOperationFinishesActivity();
	}

	@Test public void testDirtyCanSave() throws IOException {
		fillInEverything();
		editor.close();
		editor.saveFromDirtyDialog();
		checkEverythingSaved();
		lastOperationFinishesActivity();
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
		lastOperationFinishesActivity();
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
		private final @StringRes int otherKeywords;
		private final @StringRes int defaultType;

		public BelongingValues(
				String name,
				String otherName,
				@StringRes int type,
				@StringRes int otherType,
				@StringRes int otherKeywords,
				@StringRes int defaultType
		) {
			this.name = name;
			this.otherName = otherName;
			this.type = type;
			this.otherType = otherType;
			this.otherKeywords = otherKeywords;
			this.defaultType = defaultType;
		}
		public String getName() {
			return name;
		}
		public String getOtherName() {
			return otherName;
		}
		public @StringRes int getType() {
			return type;
		}
		public @StringRes int getOtherType() {
			return otherType;
		}
		public @StringRes int getOtherKeywords() {
			return otherKeywords;
		}
		public @StringRes int getDefaultType() {
			return defaultType;
		}

		protected abstract DataBaseActor.BelongingAssertions createAssertions(DataBaseActor database);
	}
}
