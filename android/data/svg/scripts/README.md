Commands to upload this module to Firebase Testlab and generate all SVGs
are located in this folder.

 * `testlab_svg_grab_artifacts.bat`
   downloads the captured `svg_xx.zip` files.
 * `testlab_svg_run.bat`
   runs an APK (hardcoded in .bat file) on Testlab on all API levels.
   Log output is saved to `gcloud-firebase-test-android-run.log`.
 * `testlab_svg_run_grab_artifacts.bat`
   parses the `testlab_svg_run.bat` output and grabs artifacts from the bucket logged.
   Side effect is the bucket name in `gcloud-firebase-test-android-run.txt`
