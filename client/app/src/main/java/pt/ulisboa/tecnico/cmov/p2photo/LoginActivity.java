package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Context;
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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initializeButtons();
    }

    private void initializeButtons() {
        /*
         * LOGIN button
         */
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginTask(LoginActivity.this).execute();

                // Login is working at the server. Run the server and use it please.

                /*Intent intent = new Intent(LoginActivity.this, DriveLogin.class);
                startActivity(intent);*/
            }
        });
        /*
         * Button that opens the SIGN IN activity
         */
        Button buttonSignIn = findViewById(R.id.button_signIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
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
    private static class LoginTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<LoginActivity> activityReference;
        private ProgressDialog pd;
        Context ctx;

        private LoginTask(LoginActivity activity) {

            activityReference = new WeakReference<>(activity);
            ctx=activity.getApplicationContext();
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

            GlobalClass context = (GlobalClass) activityReference.get().getApplicationContext();
            ServerConnection conn = context.getConnection();

            if (!ServerConnection.isOnline(context)) {
                Log.d("LoginActivity", ctx.getString(R.string.network_disabled));

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, ctx.getString(R.string.network_disabled), Toast.LENGTH_LONG).show();
                    }
                });

                return false;
            }

            try {
                conn.connect();
                Log.d("LoginActivity", "Connected to: " + conn.getAddress());
            } catch (IOException e) {
                conn.disconnect();

                Log.d("LoginActivity", ctx.getString(R.string.server_connect_fail));
                Log.d("LoginActivity", e.getMessage());

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, ctx.getString(R.string.server_connect_fail), Toast.LENGTH_LONG).show();
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
                Log.d("LoginActivity", ctx.getString(R.string.user_pass_empty));

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, ctx.getString(R.string.user_pass_empty), Toast.LENGTH_LONG).show();
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

                Log.d("LoginActivity", ctx.getString(R.string.server_contact_fail));

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, ctx.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show();
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

                Log.d("LoginActivity", ctx.getString(R.string.login_invalid));
                Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.login_invalid), Toast.LENGTH_LONG).show();
            }
        }
    }
}
