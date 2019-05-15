package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it in Google Drive. The
 * user is prompted with a pre-made dialog which allows them to choose the file location.
 */
public class DriveLoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private GoogleSignInAccount account;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drive_login);

        com.google.android.gms.common.SignInButton clickButton = findViewById(R.id.sign_in_button);
        clickButton.setOnClickListener((View v) -> {
            switch (v.getId()) {
                case R.id.sign_in_button:
                    signIn();
                    //Intent intent = new Intent(DriveLoginActivity.this, DriveActions.class);
                    //startActivity(intent);
                    break;
            }
        });
    }

    /**
     * Start sign in activity.
     */
    private void signIn() {
        Log.i("DriveLoginActivity", "Start sign in");
        GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Build a Google SignIn client.
     */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestEmail()
                        .requestProfile()
                        .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        return googleSignInClient;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i("DriveLoginActivity", "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i("DriveLoginActivity", "Signed in successfully.");

                    account = GoogleSignIn.getLastSignedInAccount(this);


                    // Use the last signed in account here since it already have a Drive scope.
                    mDriveClient = Drive.getDriveClient(this, account);
                    // Build a drive resource client.
                    mDriveResourceClient =
                            Drive.getDriveResourceClient(this, account);

                    setDriveVars();

                    Intent intent = new Intent(DriveLoginActivity.this, LoggedInActivity.class);
                    startActivity(intent);
                }
        }
    }

    void setDriveVars() {
        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Set mDriveClient and mDriveResourceCliente in global/application context
        globalVariable.setDriveClient(mDriveClient);
        globalVariable.setDriveResourceClient(mDriveResourceClient);
        globalVariable.setGoogleAccount(account);
        globalVariable.setGoogleSignInClient(googleSignInClient);
    }
}
