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
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class LoginTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<LoginActivity> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    public LoginTask(GlobalClass ctx, LoginActivity activity) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(ctx.getString(R.string.login));
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

        ServerConnection conn = context.getConnection();

        if (!ServerConnection.isOnline(context)) {
            Log.d("LoginTask", context.getString(R.string.network_disabled));

            // https://stackoverflow.com/questions/34026903/how-runnable-is-created-from-java8-lambda
            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.network_disabled), Toast.LENGTH_LONG).show()
            );

            return false;
        }

        try {
            conn.connect();
            Log.d("LoginTask", "Connected to: " + conn.getAddress());
        } catch (IOException e) {
            conn.disconnect();

            Log.d("LoginTask", context.getString(R.string.server_connect_fail));
            Log.d("LoginTask", e.getMessage());

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, context.getString(R.string.server_connect_fail), Toast.LENGTH_LONG).show();
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
            //String msg = "Username and password cannot be empty!";
            Log.d("LoginTask", context.getString(R.string.user_pass_empty));

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, context.getString(R.string.user_pass_empty), Toast.LENGTH_LONG).show();
                }
            });

            return false;
        }

        Log.d("LoginTask", "Username: " + username);
        Log.d("LoginTask", "Password: " + password);

        try {
            return conn.login(username, password);
        } catch (IOException e) {
            conn.disconnect();

            Log.d("LoginTask", context.getString(R.string.server_contact_fail));

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show();
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

            Log.d("LoginTask", context.getString(R.string.login_invalid));
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.login_invalid), Toast.LENGTH_LONG).show();
        }
    }
}