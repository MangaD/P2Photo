package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import java.lang.ref.WeakReference;

public class LoggedInActivity extends AppCompatActivity {

    private static final String TAG = "loggedin";

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        initializeButtons();
    }

    private void initializeButtons() {
        /**
         * CREATE ALBUM
         */
        Button buttonCreateAlbum = findViewById(R.id.button_create_album);
        buttonCreateAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, CreateAlbumActivity.class);
                startActivity(intent);
            }
        });
        /**
         * FIND USERS
         */
        Button buttonFindUser = findViewById(R.id.button_find_user);
        buttonFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, FindUserActivity.class);
                startActivity(intent);
            }
        });
        /**
         * ADD PHOTO TO ALBUM
         */
        Button buttonAddPhoto = findViewById(R.id.button_add_photo);
        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setDriveVars();

                Intent intent = new Intent(LoggedInActivity.this, AddPhotoActivity.class);
                startActivity(intent);
            }
        });
        /**
         * ADD USER TO ALBUM
         */
        Button buttonAddUserToAlbum = findViewById(R.id.button_add_user_to_album);
        buttonAddUserToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, AddUserToAlbumActivity.class);
                startActivity(intent);
            }
        });
        /**
         * LIST USER'S ALBUNS
         */
        Button buttonListAlbums = findViewById(R.id.button_list_user_album);
        buttonListAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setDriveVars();

                //Intent intent = new Intent(LoggedInActivity.this, ListUserAlbumActivity.class);
                Intent intent = new Intent(LoggedInActivity.this, ListUserAlbumActivity2.class);
                startActivity(intent);
            }
        });

        /**
         * LOG OUT
         */
        Button buttonLogOut = findViewById(R.id.button_log_out);
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LogOutTask(LoggedInActivity.this).execute();
            }
        });

    }

    void setDriveVars() {
        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Set mDriveClient and mDriveResourceCliente in global/application context
        globalVariable.setmDriveClient(mDriveClient);
        globalVariable.setmDriveResourceClient(mDriveResourceClient);
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }


    /**
     * Server side tasks.
     */

    private static class LogOutTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<LoggedInActivity> activityReference;
        private ProgressDialog pd;
        private String msg = "Logged out.";

        private LogOutTask(LoggedInActivity activity) {

            activityReference = new WeakReference<>(activity);

            // Create Progress dialog
            pd = new ProgressDialog(activity);
            pd.setMessage("Logging out...");
            pd.setTitle("");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... values) {
            GlobalClass context = (GlobalClass) activityReference.get().getApplicationContext();
            ServerConnection conn = context.getConnection();

            conn.disconnect();
            Log.d("LoggedInActivity", "Disconnected");
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            pd.dismiss();
            Log.d("LoggedInActivity", msg);
            Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(activityReference.get(), LoginActivity.class);
            activityReference.get().startActivity(intent);
        }
    }
}
