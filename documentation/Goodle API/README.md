# Configure Google API

Edit `build.gradle`(Module: app) file. Add to existing `dependencies` tag:

```json
dependencies {
    implementation 'com.google.android.gms:play-services-drive:15.0.0'
    implementation 'com.google.android.gms:play-services-auth:15.0.0'
}
```

Tutorials:

https://developers.google.com/identity/sign-in/android/start-integrating

https://stackoverflow.com/questions/34187555/how-do-i-do-authenticating-your-client-with-google

https://developers.google.com/identity/sign-in/android/sign-in

## Steps

1. In [this](https://console.developers.google.com/start/api?id=drive&credential=client_key) link click on "create a project" and then "Go to credentials".
2. Which API are you using: Google Drive API
   Where will you be calling the API from? Android
   What data will you be accessing? User data
3. Set up OAuth consent screen: Set up consent screen
   - On consent page:
     Application name: P2Photo
     Authorised domains: pt.ulisboa.tecnico.cmov.p2photo
     Click "Save"
     Click "Create credentials" and "OAuth Client ID"
     Application type: Android
     Name: P2Photo
     Package name: pt.ulisboa.tecnico.cmov.p2photo
4. Create an OAuth 2.0 client ID:
   Name: P2Photo
   Package name: pt.ulisboa.tecnico.cmov.p2photo
   Then do the following commands. In my case `keytool -exportcert -v -alias androiddebugkey -keystore /home/david/.android/debug.keystore` Password is `android`. 
   And then also did: `keytool -importkeystore -srckeystore /home/david/.android/debug.keystore -destkeystore /home/david/.android/debug.keystore -deststoretype pkcs12`.
   Finally: `keytool -keystore /home/david/.android/debug.keystore -list -v` and copy the SHA1 fingerprint to the site.
5. Click done.
6. In `My Project` dashboard click on "Enable APIs and Services"
7. Search and pick `Google Drive API`

In my case I can see the OAuth 2.0 client ID that I created on this page:
<https://console.developers.google.com/apis/credentials?project=vocal-raceway-237919>

And the Google Drive API here:
<https://console.developers.google.com/apis/api/drive.googleapis.com/overview?project=vocal-raceway-237919>

