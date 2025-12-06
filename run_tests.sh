#!/bin/bash
echo "Starting tests at $(date)" > test_log.txt
echo "Checking devices..." >> test_log.txt
/Users/gimjiho/Library/Android/sdk/platform-tools/adb devices >> test_log.txt 2>&1
echo "Running connectedAndroidTest..." >> test_log.txt
./gradlew connectedAndroidTest >> test_log.txt 2>&1
echo "Done at $(date)" >> test_log.txt
