# Sockets in Android

In order to use Sockets, one must add the following permissions to the `AndroidManifest.xml` file (tags are children of `manifest` tag):

```xml
<!--  This permission is for Internet connection established (using sockets). -->
<uses-permission android:name="android.permission.INTERNET" />
<!--  This permission is to check weather Internet connection is available or not, not strictly necessary but highly recommended -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Sockets perform blocking operations, so blocking calls should be in an `AsyncTask`. Example:

1. Call task:

   ```java
    new LoginTask(LoginActivity.this).execute();
   ```

2. AsyncTask for Login:

```java
/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
private static class LoginTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<LoginActivity> activityReference;
    private ProgressDialog pd;

    private LoginTask(LoginActivity activity) {

        activityReference = new WeakReference<>(activity);

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage("Logging in...");
        pd.setTitle("");
        pd.setIndeterminate(true);
        pd.setCancelable(false);
    }

    /**
     * onPreExecute called before the doInBackgroud start to display progress dialog.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Show Progress dialog
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... values) {

        GlobalClass context = (GlobalClass) activityReference.get().getApplicationContext();
        ServerConnection conn = context.getConnection();

        if (!ServerConnection.isOnline(context)) {
            String msg = "Network is disabled.";
            Log.d("LoginActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return false;
        }

        try {
            conn.connect();
            Log.d("LoginActivity", "Connected to: " + conn.getAddress());
        } catch (IOException e) {
            conn.disconnect();
            String msg = "Failed to connect to the server.";
            Log.d("LoginActivity", msg);
            Log.d("LoginActivity", e.getMessage());

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return false;
        }

        EditText usernameEdit = activityReference.get().findViewById(R.id.login_username);
        String username = usernameEdit.getText().toString();
        EditText passwordEdit = activityReference.get().findViewById(R.id.login_password);
        String password = passwordEdit.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            conn.disconnect();
            String msg = "Username and password cannot be empty!";
            Log.d("LoginActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return false;
        }

        Log.d("LoginActivity", "Username: " + username);
        Log.d("LoginActivity", "Password: " + password);

        try {
            return conn.login(username, password);
        } catch (IOException e) {
            conn.disconnect();
            String msg = "Failed to contact the server.";
            Log.d("LoginActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return false;
        }
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(Boolean result) {
        pd.dismiss();
        if (result) {
            Intent intent = new Intent(activityReference.get(), DriveLogin.class);
            activityReference.get().startActivity(intent);
        } else {
            String msg = "Login invalid.";
            Log.d("LoginActivity", msg);
            Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    }
}
```

