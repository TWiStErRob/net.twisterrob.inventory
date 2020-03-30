@echo off
rem Script assumes gcloud on path, authenticated and project set (see FIREBASE_TESTLAB.md)
setlocal
rem gsutil mb -p twisterrob-inventory --retention 1d gs://android-test
rem + gcloud firebase test android run --

rem https://cloud.google.com/sdk/gcloud/reference/firebase/test/android/run
call gcloud firebase test android run ^
  --type instrumentation ^
  --app I:\android\data\testlab\build\outputs\apk\debug\net.twisterrob.inventory.data.debug@10003064-v1.0.0#3064d+debug.apk ^
  --test I:\android\data\testlab\build\outputs\apk\androidTest\debug\net.twisterrob.inventory.data.debug.test@10003064-v1.0.0#3064d+debug-androidTest.apk ^
  --device model=m0,version=18,locale=en,orientation=portrait ^
  --device model=g3,version=19,locale=en,orientation=portrait ^
  --device model=hammerhead,version=21,locale=en,orientation=portrait ^
  --device model=A0001,version=22,locale=en,orientation=portrait ^
  --device model=zeroflte,version=23,locale=en,orientation=portrait ^
  --device model=HWMHA,version=24,locale=en,orientation=portrait ^
  --device model=cheryl,version=25,locale=en,orientation=portrait ^
  --device model=natrium,version=26,locale=en,orientation=portrait ^
  --device model=taimen,version=27,locale=en,orientation=portrait ^
  --device model=dipper,version=28,locale=en,orientation=portrait ^
  --device model=k61v1_basic_ref,version=29,locale=en,orientation=portrait ^
  --directories-to-pull /sdcard/Android/data/net.twisterrob.inventory.data.debug/files/svg ^
  1> gcloud-firebase-test-android-run.log ^
  2>&1

rem For some reason this is invalid:
rem     --directories-to-pull /data/data/net.twisterrob.inventory.data.debug/cache/svg
rem     --directories-to-pull /data/net.twisterrob.inventory.data.debug/cache/svg
rem so the test manually copies the a file to Context.getExternalFilesDir
rem (Environment.externalStorageDirectory is not writable on 29 for some reason, even with permissions granted.)
rem (/data/data is not always the same, on API 29 it's /data/user/0/<package ID>/)
