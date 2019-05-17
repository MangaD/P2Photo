package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.SignUpActivity;

import pt.ulisboa.tecnico.cmov.p2photo.security.AsymmetricEncryption;
import pt.ulisboa.tecnico.cmov.p2photo.security.Utility;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate' and 'publishProgress'
 * 3rd - argument type of 'onPostExecute'
 */
public class SignUpTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "SignUpTask";

    private GlobalClass context;
    private WeakReference<SignUpActivity> activityReference;
    private ProgressDialog pd;
    private Context ctx;
    //private String successMsg = "Sign up successful!";

    public SignUpTask(GlobalClass context, SignUpActivity activity) {

        this.context = context;
        activityReference = new WeakReference<>(activity);
        ctx = activity.getApplicationContext();

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(ctx.getString(R.string.signing_up));
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
    protected String doInBackground(Void... values) {

        ServerConnection conn = context.getServerConnection();

        if (!ServerConnection.isOnline(context)) {
            Log.d(TAG, ctx.getString(R.string.network_disabled));

            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, ctx.getString(R.string.network_disabled), Toast.LENGTH_LONG).show()
            );

            return ctx.getString(R.string.network_disabled);
        }

        try {
            conn.connect();
            Log.d(TAG, "Connected to: " + conn.getAddress());
        } catch (IOException e) {
            conn.disconnect();
            Log.d(TAG, ctx.getString(R.string.server_connect_fail));
            Log.d(TAG, e.getMessage());

            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, ctx.getString(R.string.server_connect_fail), Toast.LENGTH_LONG).show()
            );

            return ctx.getString(R.string.server_connect_fail);
        }

        EditText usernameEdit = activityReference.get().findViewById(R.id.signin_username);
        String username = usernameEdit.getText().toString();
        EditText passwordEdit = activityReference.get().findViewById(R.id.signin_password);
        String password = passwordEdit.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            conn.disconnect();
            Log.d(TAG, ctx.getString(R.string.user_pass_empty));

            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, ctx.getString(R.string.user_pass_empty), Toast.LENGTH_LONG).show()
            );

            return ctx.getString(R.string.user_pass_empty);
        }

        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Password: " + password);

        String encPrivKeyBase64;
        String pubKeyBase64;
        String passwordBase64;
        try {
            KeyPair keys = AsymmetricEncryption.generateKeyPair();
            byte[] encPrivKey = AsymmetricEncryption.encryptPrivateKey(keys.getPrivate(), password);
            encPrivKeyBase64 = Utility.bytesToBase64(encPrivKey);
            pubKeyBase64 = Utility.bytesToBase64(AsymmetricEncryption.publicKeyToByteArray(keys.getPublic()));
            // Has password with SHA-512 to be different than the hash that is used to cipher the private password
            passwordBase64 = Utility.passwordToSHA512Base64(password);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            conn.disconnect();
            Log.d(TAG, ctx.getString(R.string.failed_generate_keys));
            Log.e(TAG, e.getMessage());
            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, ctx.getString(R.string.failed_generate_keys), Toast.LENGTH_LONG).show()
            );
            return ctx.getString(R.string.failed_generate_keys);
        }

        try {
            return conn.signup(username, passwordBase64, pubKeyBase64, encPrivKeyBase64);
        } catch (IOException e) {
            conn.disconnect();

            Log.d(TAG, ctx.getString(R.string.server_contact_fail));

            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, ctx.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
            );

            return ctx.getString(R.string.server_contact_fail);
        }
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(String msg) {
        pd.dismiss();
        Log.d(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        if (msg.equals(ctx.getString(R.string.sign_up_success))) {
            Intent intent = new Intent(activityReference.get(), LoginActivity.class);
            activityReference.get().startActivity(intent);
        }
    }
}