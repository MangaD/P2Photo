package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.MainMenuActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;
import pt.ulisboa.tecnico.cmov.p2photo.security.AsymmetricEncryption;
import pt.ulisboa.tecnico.cmov.p2photo.security.Utility;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class LoginTask extends AsyncTask<Void, Void, Boolean> {

    public static final String TAG = "LoginTask";

    private WeakReference<LoginActivity> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    public LoginTask(GlobalClass ctx, LoginActivity activity) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(ctx.getString(R.string.loggin_in));
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

        /*
         * Login to drive
         */
        if (context.getStorageMode().equals(context.getString(R.string.storage_default))) {
            activityReference.get().runOnUiThread(() ->
                    activityReference.get().signInGoogleDrive()
            );
            try {
                activityReference.get().driveSemaphore.acquire();
            } catch (InterruptedException e) {
                Log.d(TAG, "Error with thread synchronization.");
                return false;
            }
            if (!activityReference.get().getIsSignedInToDrive()) {
                activityReference.get().runOnUiThread(() ->
                        Toast.makeText(context, context.getString(R.string.failed_drive_login), Toast.LENGTH_LONG).show()
                );
                return false;
            }
        }

        /*
         * Login to server
         */
        ServerConnection conn = context.getServerConnection();

        if (!ServerConnection.isOnline(context)) {
            Log.d(TAG, context.getString(R.string.network_disabled));

            // https://stackoverflow.com/questions/34026903/how-runnable-is-created-from-java8-lambda
            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.network_disabled), Toast.LENGTH_LONG).show()
            );

            return false;
        }

        try {
            conn.connect();
            Log.d(TAG, "Connected to: " + conn.getAddress());
        } catch (IOException e) {
            conn.disconnect();

            Log.d(TAG, context.getString(R.string.server_connect_fail));
            Log.d(TAG, e.getMessage());

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.server_connect_fail), Toast.LENGTH_LONG).show()
            );

            return false;
        }

        EditText usernameEdit = activityReference.get().findViewById(R.id.login_username);
        String username = usernameEdit.getText().toString();
        EditText passwordEdit = activityReference.get().findViewById(R.id.login_password);
        String password = passwordEdit.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            conn.disconnect();
            //String msg = "Username and password cannot be empty!";
            Log.d(TAG, context.getString(R.string.user_pass_empty));

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.user_pass_empty), Toast.LENGTH_LONG).show()
            );

            return false;
        }

        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);

        try {
            String[] pair = conn.login(username, password);
            if(pair == null || pair[0].isEmpty() || pair[1].isEmpty()) {
                Log.d(TAG, context.getString(R.string.login_invalid));
                activityReference.get().runOnUiThread(() ->
                    Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.login_invalid), Toast.LENGTH_LONG).show()
                );
                return false;
            } else {
                try {
                    // Set public key in application context
                    String pubKeyBase64 = pair[1];
                    byte[] pubKey = Utility.base64ToBytes(pubKeyBase64);
                    context.setPubKey(AsymmetricEncryption.publicKeyFromByteArray(pubKey));
                    // Set private key in application context
                    String encPrivKeyBase64 = pair[0];
                    byte[] encPrivKey = Utility.base64ToBytes(encPrivKeyBase64);
                    //TODO decrypt private key, use KeyStore
                    context.setPrivKey(AsymmetricEncryption.privateKeyFromByteArray(encPrivKey));
                    return true;
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    activityReference.get().runOnUiThread(() ->
                            Toast.makeText(activityReference.get().getApplicationContext(), "Problem reading private key.", Toast.LENGTH_LONG).show()
                    );
                    return false;
                }
            }
        } catch (IOException e) {
            conn.disconnect();

            Log.d(TAG, context.getString(R.string.server_contact_fail));

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
            );

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
            Intent intent = new Intent(activityReference.get(), MainMenuActivity.class);
            activityReference.get().startActivity(intent);
        }
    }
}