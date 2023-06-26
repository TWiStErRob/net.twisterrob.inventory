#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 API_LEVEL" >&2
    exit 1
fi

# Similar to GitHub actions: https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsshell
set -euxo pipefail

API_LEVEL=$1
adb shell "content query --uri content://media/external/file --projection _data" || true
adb shell "readlink -f /sdcard" || true
adb shell "ls -la /sdcard" || true
adb shell "ls -la /sdcard/" || true
adb shell "ls -la /sdcard/Android" || true
adb shell "ls -la /sdcard/Android/data" || true
adb shell "ls -la /storage/emulated/0" || true
adb shell "ls -la /storage/emulated/0/Android" || true
if [ "${API_LEVEL}" -lt 29 ]; then
    # Satisfy AGP's findAdditionalTestOutputDirectoryOnDevice -> queryAdditionalTestOutputLocation
    # so API 21-28 can generate additional output.
    adb shell "mkdir -p /sdcard/Android"
    # Tell the Android system's MediaProvider that we added a directory.
    # This will ensure that `content query --uri content://media/external/file` finds the new folder.
    adb shell "content insert --uri content://media/external/file --bind _data:s:\$(readlink -f /sdcard)/Android"
else
    echo "API 29+ (${API_LEVEL}) does not need to create /sdcard/Android"
fi
adb shell "content query --uri content://media/external/file --projection _data" || true
adb shell "readlink -f /sdcard" || true
adb shell "ls -la /sdcard" || true
adb shell "ls -la /sdcard/" || true
adb shell "ls -la /sdcard/Android" || true
adb shell "ls -la /sdcard/Android/data" || true
adb shell "ls -la /storage/emulated/0" || true
adb shell "ls -la /storage/emulated/0/Android" || true
