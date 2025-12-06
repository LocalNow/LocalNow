#!/bin/bash
echo "Starting installation at $(date)" > launch_log.txt
ADB="/Users/gimjiho/Library/Android/sdk/platform-tools/adb"
APK="app/build/outputs/apk/debug/app-debug.apk"

echo "Checking for APK..." >> launch_log.txt
ls -l $APK >> launch_log.txt 2>&1

echo "Installing APK..." >> launch_log.txt
$ADB install -r $APK >> launch_log.txt 2>&1

echo "Launching SplashActivity..." >> launch_log.txt
$ADB shell am start -n com.example.localnow/.SplashActivity >> launch_log.txt 2>&1

echo "Checking process..." >> launch_log.txt
$ADB shell pidof com.example.localnow >> launch_log.txt 2>&1

echo "Done at $(date)" >> launch_log.txt
