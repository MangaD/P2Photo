package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.p2photo.DriveConnection;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddPhotoActivity;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class AddPhotoTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "AddPhotoTask";
    
    private WeakReference<AddPhotoActivity> activityReference;
    private GlobalClass context;
    private DriveConnection driveConn;

    private ArrayList<String> albumArrayList;

    private String imageTitle = "";
    private ArrayAdapter<String> albumArrayAdapter;

    private String IndexURL = null; //to use just when album permission is added and the user
    // doesn't have the album on its drive yet

    private String albumName = null;
    private String encKeyBase64;

    public AddPhotoTask(GlobalClass ctx, AddPhotoActivity activity) {
        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        this.driveConn = ctx.getDriveConnection();
    }

    @Override
    protected String doInBackground(Void... values) {

        ServerConnection conn = context.getServerConnection();
        try {
            /**
             * Get user's allowed albums
             */
            HashMap<Integer, String> hashMap = conn.getUsersAllowedAlbums();
            ArrayList<String> list = new ArrayList<>(hashMap.values());

            if (list == null) {
                conn.disconnect();
                Log.d(TAG, context.getString(R.string.server_contact_fail));

                showMessage(context.getString(R.string.server_contact_fail));
            } else {
                this.albumArrayList = list;
                Log.i(TAG, "List size: " + list.size());
                for (String s : list) {
                    Log.i(TAG, s);
                }
            }

        } catch (Exception e) {
            Log.i(TAG, "Failed to show albuns: " + e);
        }
        return "n";
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(String msg) {
        activityReference.get().initButtons();
        createDialogOpts(albumArrayList);
    }

    private void showMessage(String msg) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        );
    }

    public void createDialogOpts(ArrayList<String> albumArrayList) {
        activityReference.get().alertDialogBuilder = new AlertDialog.Builder(activityReference.get());

        LinearLayout layout = new LinearLayout(activityReference.get());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        TextView tv = new TextView(activityReference.get());
        tv.setText("Choose Album and Insert Photo Title");
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        EditText et = new EditText(activityReference.get());
        this.imageTitle = et.getText().toString();
        TextView tv1 = new TextView(activityReference.get());
        tv1.setText("Insert Photo Title");


        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv1, tv1Params);
        layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));


        activityReference.get().alertDialogBuilder.setView(layout);
        activityReference.get().alertDialogBuilder.setTitle("Choose Album");
        // alertDialogBuilder.setMessage("Input Student ID");
        activityReference.get().alertDialogBuilder.setCustomTitle(tv);

        albumArrayAdapter = new ArrayAdapter<>(activityReference.get(), android.R.layout.simple_list_item_1, albumArrayList);
        activityReference.get().alertDialogBuilder.setAdapter(albumArrayAdapter, (DialogInterface dialog, int which) -> {
            String albumName = albumArrayAdapter.getItem(which);
            this.albumName = albumName;
            imageTitle = et.getText().toString();

            try {
                this.encKeyBase64 = context.getServerConnection().getAlbumKey(this.albumName);
            } catch (IOException e) {
                showMessage("Failed to get album key.");
                return;
            }

            saveFileToDrive();
        });
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = activityReference.get().mBitmapToSave;

        driveConn.getDriveResourceClient()
                .createContents()
                .continueWithTask(
                        task ->
                                createFileIntentSender(task.getResult(), image)
                )
                .addOnFailureListener(
                        e -> Log.w(TAG, "Failed to create new contents.", e));

    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new photo in Drive.
     */
    private Task<DriveFile> createFileIntentSender(DriveContents driveContents, Bitmap image) {
        Log.i(TAG, "ALBUM NAME: " + albumName);
        getAlbumByName(albumName, driveContents, image);
        return null;
    }

    public void getAlbumByName(String albumName, DriveContents driveContents, Bitmap image) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, albumName))
                .build();

        driveConn.getDriveResourceClient()
                .query(query)
                .addOnSuccessListener(activityReference.get(),
                        metadataBuffer -> {
                            if (metadataBuffer.getCount() == 0) {
                                Log.i(TAG, "Album '" + albumName + "' does not exist. Creating...");
                                createFolder(albumName);
                            } else {
                                Log.i(TAG, "Album '" + albumName + "' exists and image will be created!");
                                createImageInAlbum(metadataBuffer.get(0).getDriveId().asDriveFolder(), driveContents, image);
                            }
                        }
                )
                .addOnFailureListener(activityReference.get(), e -> {
                            Log.i(TAG, "Error retrieving files", e);
                            showMessage("Query album failed");
                            return;
                        }
                );
    }

    private void createFolder(String albumN) {
        driveConn.getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(task -> {
                    DriveFolder parentFolder = task.getResult();
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(albumN)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setStarred(true)
                            .build();
                    return driveConn.getDriveResourceClient().createFolder(parentFolder, changeSet);
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
                                    result = driveConn.getService().files().list()
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
                                            driveConn.getService().permissions().create(file.getId(), newPermission).execute();
                                        }
                                    }

                                } catch (Exception e) {
                                    Log.i("LINK", "Failed to add permition: " + e);
                                }
                            }).start();


                            activityReference.get().runOnUiThread(() ->
                                    Toast.makeText(context, context.getString(R.string.album_created) +
                                            driveFolder.getDriveId().encodeToString(), Toast.LENGTH_LONG).show()
                            );
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e(TAG, "Unable to create file", e);
                    activityReference.get().runOnUiThread(() ->
                            Toast.makeText(context, context.getString(R.string.file_create_error), Toast.LENGTH_LONG).show()
                    );
                    // indexSemaphore.release();
                });
    }

    private Task<DriveFile> createImageInAlbum(final DriveFolder parent, DriveContents driveContents, Bitmap image) {
        // [START drive_android_create_file]
        final Task<DriveFolder> rootFolderTask = driveConn.getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = driveConn.getDriveResourceClient().createContents();
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(task -> {
                    //DriveFolder parent = rootFolderTask.getResult();
                    DriveContents contents = createContentsTask.getResult();
                    OutputStream outputStream = contents.getOutputStream();

                    // Write the bitmap data from it.
                    ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 50, bitmapStream);
                    try {
                        outputStream.write(bitmapStream.toByteArray());
                    } catch (IOException e1) {
                        Log.i("ERROR", "Unable to write file contents.");
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(imageTitle + ".png")
                            .setMimeType("image/jpeg")
                            .build();

                    return driveConn.getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(activityReference.get(),
                        driveFile -> {

                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            getIndexByName(("index" + albumName), driveFile);

                            showMessage("Image created " +
                                    driveFile.getDriveId().encodeToString());
                            return;
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e(TAG, "Unable to create file", e);
                    return;
                });
        // [END drive_android_create_file]
        return null;
    }

    private void insertIndexFileInAlbum(String albumName, DriveFolder parent) {
        driveConn.getDriveResourceClient()
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


                    return driveConn.getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(activityReference.get(),
                        driveFile -> {
                            context.getDriveConnection().addIndexToIndexList("index" + albumName, driveFile.getDriveId()); // add to local indexes

                            activityReference.get().runOnUiThread(() ->
                                    Toast.makeText(context, context.getString(R.string.file_created) +
                                            driveFile.getDriveId().encodeToString(), Toast.LENGTH_LONG).show()
                            );

                            Log.d(TAG, context.getString(R.string.file_created) +
                                    driveFile.getDriveId().encodeToString());

                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            Task<Metadata> queryTask = driveConn.getDriveResourceClient().getMetadata(driveFile);

                            queryTask.addOnSuccessListener(activityReference.get(),
                                    Metadata -> {
                                        //String link2 = queryTask.getResult().getEmbedLink();
                                        String link2 = queryTask.getResult().getWebContentLink();
                                        Log.i(TAG, "Success getting URL Embeded " + link2);

                                        activityReference.get().runOnUiThread(() ->
                                                Toast.makeText(context, "Success getting URL " + link2,
                                                        Toast.LENGTH_LONG).show()
                                        );

                                        setIndexURL(link2);

                                        String indexURL = getIndexURL();
                                        Log.i(TAG, "URL: " + indexURL);


                                        try {
                                            context.getServerConnection().setAlbumIndex(albumName, indexURL, this.encKeyBase64);
                                        } catch (IOException e) {
                                            Log.i(TAG, "Failed to add URL Index: " + e);
                                        }

                                        //indexSemaphore.release();
                                    })
                                    .addOnFailureListener(activityReference.get(), e -> {
                                        Log.i(TAG, "Error getting URL");

                                        activityReference.get().runOnUiThread(() ->
                                                Toast.makeText(context, "Error getting URL",
                                                        Toast.LENGTH_LONG).show()
                                        );

                                        //indexSemaphore.release();
                                    });
                            return;
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e(TAG, "Unable to create file", e);

                    activityReference.get().runOnUiThread(() ->
                            Toast.makeText(context, context.getString(R.string.file_create_error),
                                    Toast.LENGTH_LONG).show()
                    );
                    //indexSemaphore.release();
                });
    }

    public void getIndexByName(String indexName, DriveFile img) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, indexName))
                .build();

        Task<MetadataBuffer> queryTask =
                driveConn.getDriveResourceClient()
                        .query(query)
                        .addOnSuccessListener(activityReference.get(),
                                metadataBuffer -> {
                                    Log.i(TAG, "Album ID: " + metadataBuffer.get(0).getDriveId());

                                    DriveFile indexFile = metadataBuffer.get(0).getDriveId().asDriveFile();

                                    Task<Metadata> queryTsk = driveConn.getDriveResourceClient().getMetadata(img);
                                    queryTsk
                                            .addOnSuccessListener(activityReference.get(),
                                                    Metadata -> {
                                                        //String link2 = queryTsk.getResult().getEmbedLink();
                                                        String link2 = queryTsk.getResult().getWebContentLink();
                                                        Log.i("LINK", "Success getting URL Embeded " + link2);
                                                        //showMessage("Success getting URL " + link2);
                                                        appendContents(indexFile, link2);
                                                    })
                                            .addOnFailureListener(activityReference.get(), e -> {
                                                Log.i("LINK", "Error getting URL");
                                                showMessage("Error getting URL");
                                                return;
                                            });
                                }
                        )
                        .addOnFailureListener(activityReference.get(), e -> {
                            Log.e(TAG, "Error retrieving files", e);
                            showMessage("Query album failed");
                            return;
                        });
    }

    private void appendContents(DriveFile file, String link) {

        // [START drive_android_open_for_append]
        Task<DriveContents> openTask =
                driveConn.getDriveResourceClient().openFile(file, DriveFile.MODE_READ_WRITE);
        // [END drive_android_open_for_append]
        // [START drive_android_append_contents]
        openTask.continueWithTask(task -> {
            DriveContents driveContents = task.getResult();
            ParcelFileDescriptor pfd = driveContents.getParcelFileDescriptor();
            long bytesToSkip = pfd.getStatSize();
            try (InputStream in = new FileInputStream(pfd.getFileDescriptor())) {
                // Skip to end of file
                while (bytesToSkip > 0) {
                    long skipped = in.skip(bytesToSkip);
                    bytesToSkip -= skipped;
                }
            }
            try (OutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                //out.write("Hello world\n".getBytes());
                out.write((link + "\n").getBytes());
            }
            // [START drive_android_commit_contents_with_metadata]
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setStarred(true)
                    .setLastViewedByMeDate(new Date())
                    .build();
            Task<Void> commitTask =
                    driveConn.getDriveResourceClient().commitContents(driveContents, changeSet);
            // [END drive_android_commit_contents_with_metadata]
            return commitTask;
        })
                .addOnSuccessListener(activityReference.get(),
                        aVoid -> {
                            showMessage("Content updated");
                            return;
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e(TAG, "Unable to update contents", e);
                    showMessage("Content update failed");
                    return;
                });
        // [END drive_android_append_contents]
    }

    private void setIndexURL(String url) {
        this.IndexURL = url;
    }

    private String getIndexURL() {
        return this.IndexURL;
    }

}