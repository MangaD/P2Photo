package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initializeButtons();
    }

    private void initializeButtons() {
        /*
        * button to confirm the creation of a user
        * */
        Button buttonSignIn = findViewById(R.id.button_create_account);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SignInTask(SignInActivity.this).execute();
            }
        });
    }

    /**
     * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
     * https://www.androidstation.info/networkonmainthreadexception/
     * <p>
     * Template meaning:
     * 1st - argument type of 'doInBackground'
     * 2nd - argument type of 'onProgressUpdate'
     * 3rd - argument type of 'onPostExecute'
     */
    private static class SignInTask extends AsyncTask<Void, Void, String> {

        private WeakReference<SignInActivity> activityReference;
        private ProgressDialog pd;
        private String successMsg = "Sign in successful!";

        private SignInTask(SignInActivity activity) {

            activityReference = new WeakReference<>(activity);

            // Create Progress dialog
            pd = new ProgressDialog(activity);
            pd.setMessage("Signing in...");
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

            GlobalClass context = (GlobalClass) activityReference.get().getApplicationContext();
            ServerConnection conn = context.getConnection();

            if (!ServerConnection.isOnline(context)) {
                String msg = "Network is disabled.";
                Log.d("SignInActivity", msg);

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                });

                return msg;
            }

            try {
                conn.connect();
                Log.d("SignInActivity", "Connected to: " + conn.getAddress());
            } catch (IOException e) {
                conn.disconnect();
                String msg = "Failed to connect to the server.";
                Log.d("SignInActivity", msg);
                Log.d("SignInActivity", e.getMessage());

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
                Log.d("SignInActivity", msg);

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                });

                return msg;
            }

            Log.d("SignInActivity", "Username: " + username);
            Log.d("SignInActivity", "Password: " + password);

            try {
                return conn.signin(username, password);
            } catch (IOException e) {
                conn.disconnect();
                String msg = "Failed to contact the server.";
                Log.d("SignInActivity", msg);

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
            Log.d("SignInActivity", msg);
            Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            if (msg.equals(successMsg)) {
                Intent intent = new Intent(activityReference.get(), SignInActivity.class);
                activityReference.get().startActivity(intent);
            }
        }
    }
}
