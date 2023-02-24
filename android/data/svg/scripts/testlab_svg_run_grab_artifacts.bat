@echo off
rem Script assumes gcloud on path, authenticated and project set (see FIREBASE_TESTLAB.md)
setlocal

echo Find bucket to pull files from.
rem   > Uploading [I:\android\data\testlab\build\outputs\apk\debug\net.twisterrob.inventory.data.debug@10002862-v1.0.0#2862d+debug.apk] to Firebase Test Lab...
rem   > Uploading [I:\android\data\testlab\build\outputs\apk\androidTest\debug\net.twisterrob.inventory.data.debug.test@10002862-v1.0.0#2862d+debug-androidTest.apk] to Firebase Test Lab...
rem   > Raw results will be stored in your GCS bucket at [https://console.developers.google.com/storage/browser/test-lab-2x2qijpmc15xa-ntvj7vsua02ni/2020-03-21_12:51:46.615000_aCPy/]
rem   > Test [matrix-2iyescjxx6mxu] has been created in the Google Cloud.
call grep -Po ^
  "(?<=Raw results will be stored in your GCS bucket at \[https://console.developers.google.com/storage/browser/").*(?=/\]")" ^
  gcloud-firebase-test-android-run.log ^
  > gcloud-firebase-test-android-run.txt
rem "test-lab-2x2qijpmc15xa-ntvj7vsua02ni/2020-03-21_12:51:46.615000_aCPy" from the URL is stored in the file.
echo Get bucket from grep result.
set /p BUCKET= < gcloud-firebase-test-android-run.txt

call testlab_svg_grab_artifacts.bat %BUCKET%
