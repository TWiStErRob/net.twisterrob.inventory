#!/usr/bin/env bash
set -euo pipefail

# AGP 9.4.1's test engine invokes `adb shell am get-current-user` on API 21.
# That Android version prints `am` usage instead of a user ID, which AGP then
# inserts into output paths. The CI emulator uses the owner user (0).
args=("$@")
for ((index = 0; index < ${#args[@]}; index++)); do
    if [[ "${args[index]}" == "shell" ]]; then
        remaining=("${args[@]:index+1}")
        if [[ "${remaining[*]}" == "am get-current-user" ]]; then
            printf '0\n'
            exit 0
        fi
    fi
done

exec "${0}.real" "$@"
