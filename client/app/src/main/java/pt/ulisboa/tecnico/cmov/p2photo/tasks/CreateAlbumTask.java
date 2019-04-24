package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.CreateAlbumActivity;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class CreateAlbumTask extends AsyncTask<Void, Void, String> {

    private WeakReference<CreateAlbumActivity> activityReference;
    private ProgressDialog pd;
    private String albumName;
    private GlobalClass context;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private String IndexURL = null;

    private Semaphore indexSemaphore = new Semaphore(0);

    public CreateAlbumTask(GlobalClass ctx, CreateAlbumActivity activity) {

        activityReference = new WeakReference<>(activity);

        this.context = ctx;

        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = context.getmDriveClient();
        this.mDriveResourceClient = context.getmDriveResourceClient();

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

        ServerConnection conn = context.getConnection();

        EditText albumNameEdit = activityReference.get().findViewById(R.id.albumName);
        albumName = albumNameEdit.getText().toString();

        if (albumName.isEmpty()) {
            conn.disconnect();
            String msg = context.getString(R.string.album_empty_name);
            Log.d("CreateAlbumTask", msg);

            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            );

            return msg;
        }

        Log.d("CreateAlbumTask", "Album name: " + albumName);

        try {
            // Add album to server
            String msg = conn.createAlbum(albumName);

            // Add album to drive
            if (msg.equals(context.getString(R.string.album_create_success))) {
                //globalVariable.addAlbumToAlbumList(albumName); //saves the name of the album locally

                boolean exists = this.albumNameExists(albumName);
                Log.d("CreateAlbumTask", Boolean.toString(exists));
                if (!exists) {
                    this.createFolder(albumName);
                }
            }

            try {
                indexSemaphore.acquire();
            } catch (InterruptedException e) {
                return "Error with thread synchronization.";
            }

            String indexURL = getIndexURL();
            Log.d("CreateAlbumTask", "indexURL: " + indexURL);

            // Add index of album to server
            msg = conn.setAlbumIndex(albumName, indexURL);

            return msg;
        } catch (IOException e) {
            conn.disconnect();
            String msg = context.getString(R.string.server_connect_fail);
            Log.d("CreateAlbumTask", msg);

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            );

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
        Log.d("CreateAlbumTask", msg);
        Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        activityReference.get().finish();
    }

    /**
     * Check if albumName already exists.
     */
    private boolean albumNameExists(String albumN) {
        GlobalClass.PhotoAlbum photoAlbum = context.findPhotoAlbum(albumN);
        if (photoAlbum == null) {
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
                .addOnSuccessListener(activityReference.get(),
                        driveFolder -> {
                            context.addAlbumToAlbumList(albumN, driveFolder.getDriveId()); // add to local albums
                            insertIndexFileInAlbum(albumN, driveFolder.getDriveId().asDriveFolder());
                            Log.d("CreateAlbumTask", context.getString(R.string.album_created) +
                                    driveFolder.getDriveId().encodeToString());

                            activityReference.get().runOnUiThread(() ->
                                    Toast.makeText(context, context.getString(R.string.album_created) +
                                            driveFolder.getDriveId().encodeToString(), Toast.LENGTH_LONG).show()
                            );
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e("CreateAlbumTask", "Unable to create file", e);
                    activityReference.get().runOnUiThread(() ->
                            Toast.makeText(context, context.getString(R.string.file_create_error), Toast.LENGTH_LONG).show()
                    );
                    indexSemaphore.release();
                });
    }

    private void insertIndexFileInAlbum(String albumName, DriveFolder parent) {
        getDriveResourceClient()
                .createContents()
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        writer.write("Inserir URLS\n");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("index" + albumName)
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .build();


                    return getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(activityReference.get(),
                        driveFile -> {
                            context.addIndexToIndexList("index" + albumName, driveFile.getDriveId()); // add to local indexes

                            activityReference.get().runOnUiThread(() ->
                                    Toast.makeText(context, context.getString(R.string.file_created) +
                                            driveFile.getDriveId().encodeToString(), Toast.LENGTH_LONG).show()
                            );

                            Log.d("CreateAlbumTask", context.getString(R.string.file_created) +
                                    driveFile.getDriveId().encodeToString());

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Task<Metadata> queryTask = getDriveResourceClient().getMetadata(driveFile);

                            queryTask.addOnSuccessListener(activityReference.get(),
                                    Metadata -> {
                                        String link2 = queryTask.getResult().getEmbedLink();
                                        Log.i("CreateAlbumTask", "Success getting URL Embeded " + link2);

                                        activityReference.get().runOnUiThread(() ->
                                                Toast.makeText(context, "Success getting URL " + link2,
                                                        Toast.LENGTH_LONG).show()
                                        );

                                        setIndexURL(link2);
                                        Log.i("CreateAlbumTask", "URL: " + getIndexURL());

                                        indexSemaphore.release();
                                    })
                                    .addOnFailureListener(activityReference.get(), e -> {
                                        Log.i("CreateAlbumTask", "Error getting URL");

                                        activityReference.get().runOnUiThread(() ->
                                                Toast.makeText(context, "Error getting URL",
                                                        Toast.LENGTH_LONG).show()
                                        );

                                        indexSemaphore.release();
                                    });

                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e("CreateAlbumTask", "Unable to create file", e);

                    activityReference.get().runOnUiThread(() ->
                            Toast.makeText(context, context.getString(R.string.file_create_error),
                                    Toast.LENGTH_LONG).show()
                    );

                    indexSemaphore.release();

                });

    }

    private DriveClient getDriveClient() {
        return this.mDriveClient;
    }

    private DriveResourceClient getDriveResourceClient() {
        return this.mDriveResourceClient;
    }

    private void setIndexURL(String iurl) {
        this.IndexURL = iurl;
    }

    private String getIndexURL() {
        return this.IndexURL;
    }
}