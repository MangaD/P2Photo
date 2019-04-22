package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import com.google.android.gms.tasks.Task;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.CreateAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class CreateAlbumActivity extends AppCompatActivity {

    private static final String TAG = "create_album";
    private GlobalClass globalVariable;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    public String IndexURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        // Obtain reference to application context
        globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        Button btn = findViewById(R.id.buttonCreate);

        btn.setOnClickListener((View v) ->
            new CreateAlbumTask((GlobalClass) getApplicationContext(), CreateAlbumActivity.this).execute()
        );
    }

    public boolean albumNameExists(String albumN) {
        //check if albumName already exists
        globalVariable = (GlobalClass) getApplicationContext();
        GlobalClass.PhotoAlbum photoAlbumum = globalVariable.findPhotoAlbum(albumN);
        if (photoAlbumum == null) {
            return false;
        }
        return true;
    }

    public void createFolder(String albumN) {
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
                            showMessage(getString(R.string.album_created) +
                                    driveFolder.getDriveId().encodeToString());
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
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
                                showMessage(getString(R.string.file_created) +
                                driveFile.getDriveId().encodeToString());

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Task<Metadata> queryTask = getDriveResourceClient().getMetadata(driveFile);
                                    queryTask
                                    .addOnSuccessListener(this,
                                            Metadata -> {
                                                String link2 = queryTask.getResult().getEmbedLink();
                                                Log.i("LINK", "Success getting URL Embeded " + link2);
                                                showMessage("Success getting URL " + link2);
                                                setIndexURL(link2);
                                                Log.i("LINK", "URL: " + getIndexURL());
                                            })
                                    .addOnFailureListener(this, e -> {
                                        Log.i("LINK", "Error getting URL");
                                        showMessage("Error getting URL");
                                        finish();
                                    });



                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(getString(R.string.file_create_error));
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

    public void setIndexURL(String iurl){
        this.IndexURL = iurl;
    }

    public String getIndexURL(){
        return this.IndexURL;
    }

}
