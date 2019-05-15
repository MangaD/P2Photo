package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.util.Collections;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.LogOutTask;

public class LoggedInActivity extends AppCompatActivity {

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Drive service;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getDriveConnection().getDriveClient();
        this.mDriveResourceClient = globalVariable.getDriveConnection().getDriveResourceClient();
        this.googleSignInClient = globalVariable.getDriveConnection().getGoogleSignInClient();

        initializeButtons();

        // Use the authenticated account to sign in to the Drive service.
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        globalVariable, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(globalVariable.getDriveConnection().getGoogleAccount().getAccount());

        Log.i("LINK", "EMail: " + globalVariable.getDriveConnection().getGoogleAccount().getEmail());


        this.service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Drive API Migration")
                .build();

        setDriveVars();

        Log.i("LINK", "SERVICE: " + service.files());

    }

    private void initializeButtons() {
        /**
         * CREATE ALBUM
         */
        Button buttonCreateAlbum = findViewById(R.id.button_create_album);
        buttonCreateAlbum.setOnClickListener((View view) -> {
            Intent intent = new Intent(LoggedInActivity.this, CreateAlbumActivity.class);
            startActivity(intent);
        });

        /**
         * ADD PHOTO TO ALBUM
         */
        Button buttonAddPhoto = findViewById(R.id.button_add_photo);
        buttonAddPhoto.setOnClickListener((View view) -> {
            setDriveVars();

            Intent intent = new Intent(LoggedInActivity.this, AddPhotoActivity.class);
            startActivity(intent);
        });
        /**
         * ADD USER TO ALBUM
         */
        Button buttonAddUserToAlbum = findViewById(R.id.button_add_user_to_album);
        buttonAddUserToAlbum.setOnClickListener((View view) -> {
            Intent intent = new Intent(LoggedInActivity.this, AddUserToAlbumActivity.class);
            startActivity(intent);
        });
        /**
         * LIST USER'S ALBUMS
         */
        Button buttonListAlbums = findViewById(R.id.button_list_user_album);
        buttonListAlbums.setOnClickListener((View view) -> {
            setDriveVars();

            //Intent intent = new Intent(LoggedInActivity.this, ListUserAlbumActivity.class);
            Intent intent = new Intent(LoggedInActivity.this, ListUserAlbumActivity2.class);
            startActivity(intent);
        });

        /**
         * LOG OUT
         */
        Button buttonLogOut = findViewById(R.id.button_log_out);
        buttonLogOut.setOnClickListener((View view) ->
            new LogOutTask((GlobalClass) getApplicationContext(), LoggedInActivity.this).execute()
        );

        /*
         * View Photo Test
         *
        Button buttonPhotoTest = findViewById(R.id.button_photo_test);
        buttonPhotoTest.setOnClickListener((View view) -> {
            Intent intent = new Intent(LoggedInActivity.this, ViewPhotoActivity.class);
            startActivity(intent);
        });*/

    }

    void setDriveVars() {
        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Set mDriveClient and mDriveResourceCliente in global/application context
        globalVariable.getDriveConnection().setDriveClient(mDriveClient);
        globalVariable.getDriveConnection().setDriveResourceClient(mDriveResourceClient);
        globalVariable.getDriveConnection().setService(service);
    }

}
