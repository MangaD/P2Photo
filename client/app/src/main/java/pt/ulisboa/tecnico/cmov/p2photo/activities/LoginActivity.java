package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

import java.util.Collections;
import java.util.concurrent.Semaphore;

import pt.ulisboa.tecnico.cmov.p2photo.DriveConnection;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.LoginTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private DriveConnection dc;
    private GlobalClass context;
    private boolean isSignedInToDrive = false;
    public Semaphore driveSemaphore = new Semaphore(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        context = (GlobalClass) getApplicationContext();

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        dc = context.getDriveConnection();

        initializeButtons();
    }

    private void initializeButtons() {
        /*
         * LOGIN button
         */
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener((View v) -> {
            if(!setSettingsLogIn()) {
                return;
            }
            new LoginTask((GlobalClass) getApplicationContext(), LoginActivity.this).execute();
        });
        /*
         * Button that opens the SIGN UP activity
         */
        Button buttonSignIn = findViewById(R.id.button_signIn);
        buttonSignIn.setOnClickListener((View v) -> {
            if(!setSettingsSignUp()) {
                return;
            }
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private boolean setSettingsSignUp() {
        if(!setServerAddress()) {
            return false;
        } else if(!setStorageMode()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean setSettingsLogIn() {
        if(!setSettingsSignUp()) {
            return false;
        } else if(!setSecurityPreferences()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean setSecurityPreferences() {
        String keyPassword = sp.getString("keyPassword", "");
        if (keyPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.keyPasswordEmpty), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.keyPasswordEmpty));
            return false;
        }
        context.setKeyPassword(keyPassword);
        return true;
    }

    private boolean setServerAddress() {
        ServerConnection conn = context.getServerConnection();
        String address = sp.getString("address", getString(R.string.default_ip));
        conn.addr = address;

        if (conn.addr.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_address), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.invalid_address));
            return false;
        }

        String port_s = sp.getString("port", getString(R.string.default_port));
        try {
            conn.port = Integer.parseInt(port_s);
            if (conn.port <= 1023 || conn.port > 65535) {
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_port), Toast.LENGTH_LONG).show();
                Log.d("LoginActivity", getString(R.string.invalid_port));
                return false;
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_port), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.invalid_port));
            return false;
        }
        Log.d("LoginActivity", "Address: " + conn.addr + "\n Port: " + conn.port);
        return true;
    }

    private boolean setStorageMode() {
        String mode = sp.getString("storage_preference", getString(R.string.storage_default));
        Log.d("LoginActivity", mode);
        if(mode.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_mode), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.invalid_mode));
            return false;
        }
        context.setStorageMode(mode);
        return true;
    }

    /**
     * Menu
     *
     * https://stackoverflow.com/questions/47488899/how-to-show-the-settingsactivity-when-pressing-on-settings-button
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.settings);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.settings){
            startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Start sign in activity.
     */
    public void signInGoogleDrive() {
        Log.i("DriveConnection", "Start sign in");
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestEmail()
                        .requestProfile()
                        .build();
        dc.setGoogleSignInClient(GoogleSignIn.getClient(this, signInOptions));
        this.startActivityForResult(dc.getGoogleSignInClient().getSignInIntent(), DriveConnection.REQUEST_CODE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case DriveConnection.REQUEST_CODE_SIGN_IN:
                Log.i("DriveConnection", "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i("DriveConnection", "Signed in successfully.");

                    dc.setGoogleAccount(GoogleSignIn.getLastSignedInAccount(context));


                    // Use the last signed in account here since it already have a Drive scope.
                    dc.setDriveClient(Drive.getDriveClient(context, dc.getGoogleAccount()));
                    // Build a drive resource client.
                    dc.setDriveResourceClient(Drive.getDriveResourceClient(context, dc.getGoogleAccount()));

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    context, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(dc.getGoogleAccount().getAccount());

                    Log.i("LINK", "EMail: " + dc.getGoogleAccount().getEmail());

                    dc.setService(new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(),
                            JacksonFactory.getDefaultInstance(), credential)
                            .setApplicationName("Drive API Migration")
                            .build());

                    Log.i("LINK", "SERVICE: " + dc.getService().files());

                    isSignedInToDrive = true;
                }
        }
        driveSemaphore.release();
    }

    public boolean getIsSignedInToDrive() {
        return isSignedInToDrive;
    }
}
