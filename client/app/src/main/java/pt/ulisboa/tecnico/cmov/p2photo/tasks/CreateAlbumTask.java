package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.crypto.SecretKey;

import pt.ulisboa.tecnico.cmov.p2photo.DriveConnection;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.CreateAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.security.AsymmetricEncryption;
import pt.ulisboa.tecnico.cmov.p2photo.security.SymmetricEncryption;
import pt.ulisboa.tecnico.cmov.p2photo.security.Utility;

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

    public static final String TAG = "CreateAlbumTask";

    private WeakReference<CreateAlbumActivity> activityReference;
    private GlobalClass context;
    private DriveConnection dc;
    private ProgressDialog pd;

    private String albumName;
    private String IndexURL = null;

    private Semaphore indexSemaphore = new Semaphore(0);

    public CreateAlbumTask(GlobalClass ctx, CreateAlbumActivity activity) {

        activityReference = new WeakReference<>(activity);

        this.context = ctx;
        this.dc = context.getDriveConnection();

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

        ServerConnection conn = context.getServerConnection();

        EditText albumNameEdit = activityReference.get().findViewById(R.id.albumName);
        albumName = albumNameEdit.getText().toString();

        if (albumName.isEmpty()) {
            conn.disconnect();
            String msg = context.getString(R.string.album_empty_name);
            Log.d(TAG, msg);

            activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            );

            return msg;
        }

        Log.d(TAG, "Album name: " + albumName);

        try {

            // Create album in drive
            boolean exists = this.albumNameExists(albumName);
            Log.d(TAG, Boolean.toString(exists));
            if (!exists) {
                this.createFolder(albumName);
                try {
                    indexSemaphore.acquire();
                } catch (InterruptedException e) {
                    return "Error with thread synchronization.";
                }
            } else {
                return "Album with that name already exists in your drive.";
            }

            // Add album to server
            String msg = conn.createAlbum(albumName);

            // Add album to drive
            if (!msg.equals(context.getString(R.string.album_create_success))) {
                return msg;
            }

            String indexURL = getIndexURL();
            Log.d(TAG, "indexURL: " + indexURL);

            /**
             * SECURITY
             */
            // Generate encrypted symmetric key to store in server
            SecretKey cipherKey;
            String encKeyBase64;
            try {
                cipherKey = SymmetricEncryption.generateAESKey();
                Log.d(TAG, cipherKey.toString());
                byte[] keyBytes = SymmetricEncryption.secretKeyToByteArray(cipherKey);
                AsymmetricEncryption ae = new AsymmetricEncryption();
                byte[] encKey = ae.encrypt(context.getPubKey(), keyBytes);
                encKeyBase64 = Utility.bytesToBase64(encKey);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                return "Error generating symmetric key.";
            }

            // Encrypt index url
            String encryptedIndexURLBase64;
            try {
                SymmetricEncryption se = new SymmetricEncryption();
                byte[] encContent = se.encryptAES(indexURL, cipherKey);
                encryptedIndexURLBase64 = Utility.bytesToBase64(encContent);
            } catch (Exception e) {
                String errorMsg = "Error encrypting index URL.";
                showMessage(errorMsg);
                Log.e(TAG, errorMsg + "\n" + e.getMessage());
                return errorMsg;
            }
            Log.d(TAG, "Encrypted index URL: " + encryptedIndexURLBase64);

            // Add index of album to server
            msg = conn.setAlbumIndex(albumName, encryptedIndexURLBase64, encKeyBase64);

            return msg;
        } catch (IOException e) {
            conn.disconnect();
            String msg = context.getString(R.string.server_connect_fail);
            Log.d(TAG, msg);
            showMessage(msg);
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
        Log.d(TAG, msg);
        Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        activityReference.get().finish();
    }

    /**
     * Check if albumName already exists.
     */
    private boolean albumNameExists(String albumN) {
        DriveConnection.PhotoAlbum photoAlbum = context.getDriveConnection().findPhotoAlbum(albumN);
        if (photoAlbum == null) {
            return false;
        }
        return true;
    }

    private void createFolder(String albumN) {
        dc.getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(albumN)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(true)
                            .build();
                    return dc.getDriveResourceClient().createFolder(parentFolder, changeSet);
                })
                .addOnSuccessListener(activityReference.get(),
                        driveFolder -> {
                            context.getDriveConnection().addAlbumToAlbumList(albumN, driveFolder.getDriveId()); // add to local albums
                            insertIndexFileInAlbum(albumN, driveFolder.getDriveId().asDriveFolder());
                            Log.d(TAG, context.getString(R.string.album_created) +
                                    driveFolder.getDriveId().encodeToString());


                            new Thread(() -> {

                                Permission newPermission = new Permission();
                                newPermission.setType("anyone");
                                newPermission.setRole("reader");
                                try {
                                    Thread.sleep(3000);
                                    // Print the names and IDs for up to 10 files.
                                    FileList result = null;
                                    result = dc.getService().files().list()
                                            .setPageSize(100)
                                            .setFields("nextPageToken, files(id, name)")
                                            .execute();
                                    List<File> files = result.getFiles();
                                    if (files == null || files.isEmpty()) {
                                        Log.i("LINK", "No files found.");
                                    } else {
                                        Log.i("LINK", "Files:");
                                        for (File file : files) {
                                            Log.i("LINK", "%s " + file.getName() + " (%s) " + file.getId() + "\n");
                                            dc.getService().permissions().create(file.getId(), newPermission).execute();
                                        }
                                    }

                                /*String id = service.files().get("root").setFields("id").execute().getId();
                                Log.i("LNK", "ID of RootFolder: " + id);
                                service.permissions().create(id, newPermission).execute();*/

                                } catch (Exception e) {
                                    Log.i("LINK", "Failed to add permition: " + e);
                                }
                            }).start();


                            /*activityReference.get().runOnUiThread(() ->
                                    Toast.makeText(context, context.getString(R.string.album_created) +
                                            driveFolder.getDriveId().encodeToString(), Toast.LENGTH_LONG).show()
                            );*/
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(context.getString(R.string.file_create_error));
                    indexSemaphore.release();
                });
    }

    private void insertIndexFileInAlbum(String albumName, DriveFolder parent) {
        dc.getDriveResourceClient()
                .createContents()
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    OutputStream outputStream = contents.getOutputStream();
                    try (Writer writer = new OutputStreamWriter(outputStream)) {
                        //writer.write("Inserir URLS\n");
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle("index" + albumName)
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .build();


                    return dc.getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(activityReference.get(),
                        driveFile -> {
                            context.getDriveConnection().addIndexToIndexList("index" + albumName, driveFile.getDriveId()); // add to local indexes

                            /*activityReference.get().runOnUiThread(() ->
                                    Toast.makeText(context, context.getString(R.string.file_created) +
                                            driveFile.getDriveId().encodeToString(), Toast.LENGTH_LONG).show()
                            );*/

                            Log.d(TAG, context.getString(R.string.file_created) +
                                    driveFile.getDriveId().encodeToString());

                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Task<Metadata> queryTask = dc.getDriveResourceClient().getMetadata(driveFile);

                            queryTask.addOnSuccessListener(activityReference.get(),
                                    Metadata -> {
                                        //String link2 = queryTask.getResult().getEmbedLink();
                                        String link2 = queryTask.getResult().getWebContentLink();
                                        Log.i(TAG, "Success getting URL Embeded " + link2);

                                        /*activityReference.get().runOnUiThread(() ->
                                                Toast.makeText(context, "Success getting URL " + link2,
                                                        Toast.LENGTH_LONG).show()
                                        );*/

                                        setIndexURL(link2);
                                        Log.i(TAG, "URL: " + getIndexURL());

                                        indexSemaphore.release();
                                    })
                                    .addOnFailureListener(activityReference.get(), e -> {
                                        Log.i(TAG, "Error getting URL");
                                        showMessage("Error getting URL");
                                        indexSemaphore.release();
                                    });

                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage(context.getString(R.string.file_create_error));
                    indexSemaphore.release();

                });

    }

    private void setIndexURL(String url) {
        this.IndexURL = url;
    }

    private String getIndexURL() {
        return this.IndexURL;
    }

    private void showMessage(String msg) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        );
    }
}