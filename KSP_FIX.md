# KSP Version Fix

The KSP plugin version for Kotlin 2.2.20 may not be available yet. If you encounter build errors, try one of these solutions:

## Option 1: Use a compatible KSP version
Update `gradle/libs.versions.toml` and change the ksp version to match an available version. You can check available versions at: https://plugins.gradle.org/plugin/com.google.devtools.ksp

Try versions like:
- `2.2.20-1.0.9`
- `2.2.19-1.0.20` (if using Kotlin 2.2.19)

## Option 2: Temporarily use kapt
If KSP doesn't work, you can temporarily use kapt by:
1. Remove the KSP plugin from `composeApp/build.gradle.kts`
2. Add kapt plugin: `id("org.jetbrains.kotlin.kapt") version "2.2.20"`
3. Change `kspAndroid` to `kapt` in dependencies

## Option 3: Wait for KSP release
KSP versions are typically released shortly after Kotlin releases. Check the KSP releases page for updates.


