#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 API_LEVEL" >&2
    exit 1
fi

# Similar to GitHub actions: https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#jobsjob_idstepsshell
set -euxo pipefail

API_LEVEL=$1
if [ "${API_LEVEL}" -lt 29 ]; then
    # Satisfy AGP's findAdditionalTestOutputDirectoryOnDevice -> queryAdditionalTestOutputLocation
    # so API 21-28 can generate additional output.
    adb shell 'mkdir /sdcard/Android'
    # Tell the Android system's MediaProvider that we added a directory.
    # This will ensure that `content query --uri content://media/external/file` finds the new folder.
    # Note: API 21-22 does not support readlink -e.
    # Note: API 27 does not support readlink without root (21-26,28-34 is ok).
    adb shell 'content insert --uri content://media/external/file --bind _data:s:$(su root readlink -f /sdcard)/Android'
else
    echo "API 29+ (${API_LEVEL}) does not need to create /sdcard/Android"
fi
