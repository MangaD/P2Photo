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

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoginActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.SignUpActivity;

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
            Log.d("SignUpTask", ctx.getString(R.string.network_disabled));

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, ctx.getString(R.string.network_disabled), Toast.LENGTH_LONG).show()
            );

            return ctx.getString(R.string.network_disabled);
        }

        try {
            conn.connect();
            Log.d("SignUpTask", "Connected to: " + conn.getAddress());
        } catch (IOException e) {
            conn.disconnect();
            Log.d("SignUpTask", ctx.getString(R.string.server_connect_fail));
            Log.d("SignUpTask", e.getMessage());

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
            Log.d("SignUpTask", ctx.getString(R.string.user_pass_empty));

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, ctx.getString(R.string.user_pass_empty), Toast.LENGTH_LONG).show()
            );

            return ctx.getString(R.string.user_pass_empty);
        }

        Log.d("SignUpTask", "Username: " + username);
        Log.d("SignUpTask", "Password: " + password);

        try {
            return conn.signup(username, password);
        } catch (IOException e) {
            conn.disconnect();

            Log.d("SignUpTask", ctx.getString(R.string.server_contact_fail));

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
        Log.d("SignUpTask", msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        if (msg.equals(ctx.getString(R.string.sign_up_success))) {
            Intent intent = new Intent(activityReference.get(), LoginActivity.class);
            activityReference.get().startActivity(intent);
        }
    }
}