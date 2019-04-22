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

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

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

        Button btn = (Button) findViewById(R.id.buttonCreate);

        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new CreateAlbumTask(CreateAlbumActivity.this).execute();
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


    /**
     * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
     * https://www.androidstation.info/networkonmainthreadexception/
     * <p>
     * Template meaning:
     * 1st - argument type of 'doInBackground'
     * 2nd - argument type of 'onProgressUpdate'
     * 3rd - argument type of 'onPostExecute'
     */
    private static class CreateAlbumTask extends AsyncTask<Void, Void, String> {

        private WeakReference<CreateAlbumActivity> activityReference;
        private ProgressDialog pd;

        private String albumName;

        private Context ctx;

        private CreateAlbumTask(CreateAlbumActivity activity) {

            activityReference = new WeakReference<>(activity);

            ctx = activity.getApplicationContext();

            // Create Progress dialog
            pd = new ProgressDialog(activity);
            pd.setMessage(ctx.getString(R.string.create_album));
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

            EditText albumNameEdit = activityReference.get().findViewById(R.id.albumName);
            albumName = albumNameEdit.getText().toString();

            if (albumName.isEmpty()) {
                conn.disconnect();
                String msg = ctx.getString(R.string.album_empty_name);
                Log.d("CreateAlbumActivity", msg);

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                });

                return msg;
            }

            Log.d("CreateAlbumActivity", "Album name: " + albumName);

            try {
                return conn.createAlbum(albumName);
            } catch (IOException e) {
                conn.disconnect();
                String msg = ctx.getString(R.string.server_connect_fail);
                Log.d("CreateAlbumActivity", msg);

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
            Log.d("CreateAlbumActivity", msg);
            Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            if (msg.equals(ctx.getString(R.string.album_create_success))) {
                //globalVariable.addAlbumToAlbumList(albumName); //saves the name of the album locally
                if (!activityReference.get().albumNameExists(albumName)) {
                    activityReference.get().createFolder(albumName);
                }
            }
        }
    }

    public void setIndexURL(String iurl){
        this.IndexURL = iurl;
    }

    public String getIndexURL(){
        return this.IndexURL;
    }

}
