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
                //new LoginTask(LoginActivity.this).execute();

                Intent intent = new Intent(LoginActivity.this, DriveLogin.class);
                startActivity(intent);
            }
        });
        /*
         * Button that opens the SIGN IN activity
         */
        Button buttonSignIn = findViewById(R.id.button_signIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
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
            } catch (IOException e) {
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
            try {
                conn.login(username, password);
            } catch (IOException e) {
                String msg = "Failed to contact the server.";
                Log.d("LoginActivity", msg);

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                });

                return false;
            }


            return true;
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
}
