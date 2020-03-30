@echo off
rem Script assumes gcloud on path, authenticated and project set (see FIREBASE_TESTLAB.md)

setlocal
set BUCKET=%1
echo https://console.developers.google.com/storage/browser/%BUCKET%

echo Pull all ZIP files from the test run
rem   /*/ is /model-version-locale-orientation/
rem   svg_*.zip is svg_version.zip
echo gsutil cp gs://%BUCKET%/*/artifacts/svg_*.zip .
call gsutil cp gs://%BUCKET%/*/artifacts/svg_*.zip .
