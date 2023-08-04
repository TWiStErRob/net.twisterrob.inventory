Release play-by-play
--------------------

1. Fix all warnings `gradlew clean releaseCheck` should yield 0 warnings.
1. Commit all changes (`svn status` should be empty)
1. `svn update` just to be sure (double-check with `svn info`)
1. `gradlew clean :android:assembleAndroidTest :android:release` (1m before ProGuard, 1m for ProGuard)
1. Upload `p:\repos\release\android\net.twisterrob.inventory*@*.zip`:
    * `+release.apk`
      @ Developer Console
      \> Release
      \> Testing
      \> Closed Testing
      \> [Alpha](https://play.google.com/console/u/0/developers/7995455198986011414/app/4974852622245161228/tracks/4698365972867036604)
    * `proguard_mapping.txt`
      @ Developer Console
      \> Release
      \> [App bundle explorer](https://play.google.com/console/u/0/developers/7995455198986011414/app/4974852622245161228/bundle-explorer)
      \> Downloads tab
      \> Assets
      \> ReTrace mapping file
1. Update https://www.twisterrob.net/project/inventory/ with release notes.
1. Update *What's new in this version?* based on previous one.
1. Make a backup of current version on the phone with
    ```bash
    adb backup -f Inventory-pre<version>.ab -apk -noobb -noshared -nosystem net.twisterrob.inventory
    ```
    Note to restore a backup for testing use
    ```bash
    adb restore Inventory-pre<version>.ab 
    ```
1. Make sure the installed release version is the previous release, and that it has all kinds of data.
1. Publish to Alpha.
1. Check [Pre-launch Report](https://play.google.com/console/u/0/developers/7995455198986011414/app/4974852622245161228/pre-launch-report/overview)
1. Generated in about 15 minutes after upload, if errors, then start again.
1. Wait until alpha stage is propagated and update current release version on my phone.
1. Smoke test for no errors.
1. If no errors Promote to Beta or Prod with staged rollout.
1. Set up a reminder in calendar to check for crashes and bump rollout.
1. Update version number in android/build.gradle anticipating minor and commit.
1. Send out emails to people who requested features.
