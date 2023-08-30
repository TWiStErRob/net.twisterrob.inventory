For the full process see [.github/release.md](https://github.com/TWiStErRob/.github/blob/main/RELEASE.md).

# Release Process

1. Ensure clean latest working copy.
   ```shell
   git checkout main
   git pull
   git status
   ```
1. Create artifacts
   ```shell
   gradlew clean :android:assembleAndroidTest :android:release
   ```
1. Upload `%RELEASE_HOME%\android\net.twisterrob.inventory*@*.zip` (latest):
   * `net.twisterrob.inventory@*+release.apk`
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
   ```shell
   adb backup -f Inventory-pre<version>.ab -apk -noobb -noshared -nosystem net.twisterrob.inventory
   # > Now unlock your device and confirm the backup operation...
   # At this point have to press "Back up my data" on the phone really quickly.
   # If the promt is shown, it's too late and the backup will not happen regardless what the toast says.
   ```
   Note: to restore a backup for testing use
   ```shell
   adb restore Inventory-pre<version>.ab 
   ```
1. Make sure the installed release version is the previous release, and that it has all kinds of data.
1. Publish to Alpha.
   1. Check [Pre-launch Report](https://play.google.com/console/u/0/developers/7995455198986011414/app/4974852622245161228/pre-launch-report/overview)  
      Generated in about 15 minutes after upload, if errors, then start again.
   1. Wait until alpha stage is propagated and update current release version on my phone.
   1. Smoke test for no errors.
1. If no errors, Promote to Beta or Prod with staged rollout.
   1. Set up a reminder in calendar to check for crashes and bump rollout.
1. Send out emails/reply to reviews for people who requested features.
   `is:need-broadcast` label in the milestone.
   Add `is:broadcasted` label once the comms are sent out.

## Prepare next release
1. Update version number in android/build.gradle anticipating minor and commit to `main`.
