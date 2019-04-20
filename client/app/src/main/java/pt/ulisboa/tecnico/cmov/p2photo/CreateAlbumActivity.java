package pt.ulisboa.tecnico.cmov.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class CreateAlbumActivity extends AppCompatActivity {

    private static final String TAG = "create_album";
    private GlobalClass globalVariable;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        // Obtain reference to application context
        globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        EditText text = (EditText) findViewById(R.id.albumName);
        Button btn = (Button) findViewById(R.id.buttonCreate);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String albumName = text.getText().toString();

                //globalVariable.addAlbumToAlbumList(albumName); //saves the name of the album locally
                if (albumNameExists(albumName)) {
                    showMessage("Album name already exists!");
                } else {
                    createFolder(albumName);
                }
            }

        });
    }

    private boolean albumNameExists(String albumN) {
        //check if albumName already exists
        globalVariable = (GlobalClass) getApplicationContext();
        GlobalClass.PhotoAlbum photoAlbumum = globalVariable.findPhotoAlbum(albumN);
        if (photoAlbumum == null) {
            return false;
        }
        return true;
    }

    private void createFolder(String albumN) {
        getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(albumN)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(true)
                            .build();
                    return getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(this,
                        driveFolder -> {
                            globalVariable.addAlbumToAlbumList(albumN, driveFolder.getDriveId()); // add to local albums
                            insertIndexFileInAlbum(albumN, driveFolder.getDriveId().asDriveFolder());
                            showMessage("Album created " +
                                    driveFolder.getDriveId().encodeToString());
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage("File create error");
                    finish();
                });
    }

    private void insertIndexFileInAlbum(String albumNam, DriveFolder parent) {
        getDriveResourceClient()
                .createContents()
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write("Inserir URLS\n");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("index"+albumNam)
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .build();


                    return getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {
                                globalVariable.addIndexToIndexList("index"+albumNam, driveFile.getDriveId()); // add to local indexes
                                showMessage("File created" +
                                driveFile.getDriveId().encodeToString());
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage("File create error");
                });

    }


    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected DriveClient getDriveClient() {
        return this.mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return this.mDriveResourceClient;
    }

}
