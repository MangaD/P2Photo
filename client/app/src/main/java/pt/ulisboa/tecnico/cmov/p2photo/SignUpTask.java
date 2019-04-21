package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

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

    private GlobalClass context;
    private WeakReference<SignUpActivity> activityReference;
    private ProgressDialog pd;
    private String successMsg = "Sign up successful!";

    public SignUpTask(GlobalClass context, SignUpActivity activity) {

        this.context = context;
        activityReference = new WeakReference<>(activity);

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage("Signing up...");
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

        ServerConnection conn = context.getConnection();

        if (!ServerConnection.isOnline(context)) {
            String msg = "Network is disabled.";
            Log.d("SignUpActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return msg;
        }

        try {
            conn.connect();
            Log.d("SignUpActivity", "Connected to: " + conn.getAddress());
        } catch (IOException e) {
            conn.disconnect();
            String msg = "Failed to connect to the server.";
            Log.d("SignUpActivity", msg);
            Log.d("SignUpActivity", e.getMessage());

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return msg;
        }

        EditText usernameEdit = activityReference.get().findViewById(R.id.signin_username);
        String username = usernameEdit.getText().toString();
        EditText passwordEdit = activityReference.get().findViewById(R.id.signin_password);
        String password = passwordEdit.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            conn.disconnect();
            String msg = "Username and password cannot be empty!";
            Log.d("SignUpActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return msg;
        }

        Log.d("SignUpActivity", "Username: " + username);
        Log.d("SignUpActivity", "Password: " + password);

        try {
            return conn.signup(username, password);
        } catch (IOException e) {
            conn.disconnect();
            String msg = "Failed to contact the server.";
            Log.d("SignUpActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return msg;
        }
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(String msg) {
        pd.dismiss();
        Log.d("SignUpActivity", msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        if (msg.equals(successMsg)) {
            Intent intent = new Intent(activityReference.get(), SignUpActivity.class);
            activityReference.get().startActivity(intent);
        }
    }
}