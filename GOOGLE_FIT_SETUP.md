# Google Fit Integration Setup

This app integrates with Google Fit to display daily step counts. To enable this feature, you need to configure the app in Google Cloud Console.

## Prerequisites

1. A Google Cloud account
2. Access to Google Cloud Console
3. The app's package name: `org.example.project`
4. Your app's SHA-1 fingerprint (for debug builds)

## Setup Steps

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your project ID

### 2. Enable Fitness API

1. In your Google Cloud project, go to **APIs & Services** > **Library**
2. Search for "Fitness API"
3. Click on "Fitness API" and click **Enable**

### 3. Configure OAuth Consent Screen

1. Go to **APIs & Services** > **OAuth consent screen**
2. Choose **External** user type (unless you have a Google Workspace account)
3. Fill in the required information:
   - App name: "WeightIT" (or your preferred name)
   - User support email: Your email
   - Developer contact information: Your email
4. Click **Save and Continue**
5. On the **Scopes** page, click **Add or Remove Scopes**
6. Add the following scopes:
   - `https://www.googleapis.com/auth/fitness.activity.read`
   - `https://www.googleapis.com/auth/fitness.location.read` (optional)
7. Click **Update** and then **Save and Continue**
8. Add test users if your app is in testing mode
9. Complete the consent screen setup

### 4. Create OAuth 2.0 Client ID

1. Go to **APIs & Services** > **Credentials**
2. Click **Create Credentials** > **OAuth client ID**
3. Choose **Android** as the application type
4. Fill in:
   - **Name**: "WeightIT Android Client" (or any name)
   - **Package name**: `org.example.project`
   - **SHA-1 certificate fingerprint**: Your app's SHA-1 fingerprint
5. Click **Create**

### 5. Get Your SHA-1 Fingerprint

For debug builds, run:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Look for the SHA-1 fingerprint in the output.

For release builds, use your release keystore:
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
```

### 6. Add google-services.json (Optional but Recommended)

If you want to use Firebase services or have better integration:

1. Go to Firebase Console: https://console.firebase.google.com/
2. Add your Android app with package name `org.example.project`
3. Download `google-services.json`
4. Place it in `composeApp/src/androidMain/` directory
5. Add the Google Services plugin to your `build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
   }
   ```

### 7. Verify Setup

After completing the setup:

1. Build and run your app
2. When prompted, sign in with your Google account
3. Grant permissions for Google Fit data access
4. Check Google Fit app > Settings > Manage connected apps
5. Your app should appear in the list

## Troubleshooting

### App doesn't appear in Google Fit connected apps

- Verify the OAuth consent screen is configured correctly
- Ensure Fitness API is enabled
- Check that the SHA-1 fingerprint matches
- Make sure you've granted permissions in the app

### Permission request doesn't work

- Check logcat for errors: `adb logcat | grep -i "google\|fitness\|signin"`
- Verify the package name matches exactly: `org.example.project`
- Ensure the OAuth client ID is created for Android platform

### No step data retrieved

- Make sure Google Fit app is installed and has step data
- Verify permissions were granted (check in Google Fit app settings)
- Check logcat for API errors
- Ensure the date range for data request is correct

## Important Notes

- The app must be signed with the same keystore that matches the SHA-1 fingerprint registered in Google Cloud Console
- For production, you'll need to add your release keystore's SHA-1 fingerprint
- Test users need to be added to the OAuth consent screen if the app is in testing mode
- The Fitness API has usage quotas - check Google Cloud Console for limits

## References

- [Google Fit API Documentation](https://developers.google.com/fit/android/get-started)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start-integrating)
- [OAuth 2.0 Setup](https://developers.google.com/identity/protocols/oauth2)




























