package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.p2photo.DriveConnection;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.AddPhotoTask;

public class AddPhotoActivity extends AppCompatActivity {

    public static final String TAG = "AddPhotoActivity";

    private GlobalClass context;
    private DriveConnection driveConn;

    private Bitmap mBitmapToSave;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    private String itemString = "null";
    Button btnTakePicture;
    Button btnFindPicture;
    private String imageTitle = "";

    AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        this.context = (GlobalClass) getApplicationContext();
        this.driveConn = context.getDriveConnection();

        new AddPhotoTask((GlobalClass) getApplicationContext(), this).execute();
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;

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
    private Task<DriveFile> createFileIntentSender(DriveContents driveContents, Bitmap image) throws InterruptedException {

        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        Log.i(TAG, "ALBUM NAME: " + itemString);

        getAlbumByName(itemString, driveContents, image);

        return null;
    }

    public void getAlbumByName(String albumName, DriveContents driveContents, Bitmap image) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, albumName))
                .build();

        Task<MetadataBuffer> queryTask =
                driveConn.getDriveResourceClient()
                        .query(query)
                        .addOnSuccessListener(this,
                                metadataBuffer -> {
                                    Log.i(TAG, "Album ID: " + metadataBuffer.get(0).getDriveId());

                                    createImageInAlbum(metadataBuffer.get(0).getDriveId().asDriveFolder(), driveContents, image);
                                }
                        )
                        .addOnFailureListener(this, e -> {
                            Log.e(TAG, "Error retrieving files", e);
                            showMessage("Query album failed");
                            finish();
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
                    // isto é do texto
                    //try (Writer writer = new OutputStreamWriter(outputStream)) {
                    //    writer.write("Hello World!");
                    //}

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(imageTitle + ".png")
                            .setMimeType("image/jpeg")
                            .build();

                    return driveConn.getDriveResourceClient().createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> {

                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            getIndexByName(("index" + itemString), driveFile);

                            showMessage("Image created " +
                                    driveFile.getDriveId().encodeToString());
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    finish();
                });
        // [END drive_android_create_file]
        return null;
    }

    public void getIndexByName(String indexName, DriveFile img) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, indexName))
                .build();

        Task<MetadataBuffer> queryTask =
                driveConn.getDriveResourceClient()
                        .query(query)
                        .addOnSuccessListener(this,
                                metadataBuffer -> {
                                    Log.i(TAG, "Album ID: " + metadataBuffer.get(0).getDriveId());

                                    DriveFile indexFile = metadataBuffer.get(0).getDriveId().asDriveFile();

                                    Task<Metadata> queryTsk = driveConn.getDriveResourceClient().getMetadata(img);
                                    queryTsk
                                            .addOnSuccessListener(this,
                                                    Metadata -> {
                                                        //String link2 = queryTsk.getResult().getEmbedLink();
                                                        String link2 = queryTsk.getResult().getWebContentLink();
                                                        Log.i("LINK", "Success getting URL Embeded " + link2);
                                                        showMessage("Success getting URL " + link2);
                                                        appendContents(indexFile, link2);
                                                    })
                                            .addOnFailureListener(this, e -> {
                                                Log.i("LINK", "Error getting URL");
                                                showMessage("Error getting URL");
                                                finish();
                                            });
                                }
                        )
                        .addOnFailureListener(this, e -> {
                            Log.e(TAG, "Error retrieving files", e);
                            showMessage("Query album failed");
                            finish();
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
                .addOnSuccessListener(this,
                        aVoid -> {
                            showMessage("Content updated");
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to update contents", e);
                    showMessage("Content update failed");
                    finish();
                });
        // [END drive_android_append_contents]
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case DriveConnection.REQUEST_CODE_FIND_IMAGE:
                Log.i(TAG, "find image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image finded successfully.");
                    // Store the image data as a bitmap for writing later.

                    try {
                        InputStream stream = getContentResolver().openInputStream(
                                data.getData());
                        mBitmapToSave = BitmapFactory.decodeStream(stream);
                        stream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // saveFileToDrive();
                    //builder.show();

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    try {
                        alertDialog.show();
                    } catch (Exception e) {
                        // WindowManager$BadTokenException will be caught and the app would
                        // not display the 'Force Close' message
                        e.printStackTrace();
                    }

                }
                break;
            case DriveConnection.REQUEST_CODE_CAPTURE_IMAGE:
                Log.i(TAG, "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image captured successfully.");
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    Log.i(TAG, "mBitmapToSave: " + mBitmapToSave.toString());
                    //  saveFileToDrive();
                    //builder.show();

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    try {
                        alertDialog.show();
                    } catch (Exception e) {
                        // WindowManager$BadTokenException will be caught and the app would
                        // not display the 'Force Close' message
                        e.printStackTrace();
                    }

                }
                break;
            case DriveConnection.REQUEST_CODE_CREATOR:
                Log.i(TAG, "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    //startActivityForResult(
                    //      new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
        }
    }

    public void initButtons() {
        /*
         * opens the activity responsible for CREATING ALBUMS
         */
        btnFindPicture = findViewById(R.id.find_picture);
        btnFindPicture.setOnClickListener((View view) ->
                //Start getContent
                startActivityForResult(
                        new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), DriveConnection.REQUEST_CODE_FIND_IMAGE)
        );
        /*
         * add photo to album
         * */
        btnTakePicture = findViewById(R.id.take_picture);
        btnTakePicture.setOnClickListener((View view) ->
                // Start camera.
                startActivityForResult(
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE), DriveConnection.REQUEST_CODE_CAPTURE_IMAGE)
        );
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void createDialogOpts(ArrayList<String> albumArrayList) {
        alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        TextView tv = new TextView(this);
        tv.setText("Choose Album and Insert Photo Title");
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        EditText et = new EditText(this);
        imageTitle = et.getText().toString();
        TextView tv1 = new TextView(this);
        tv1.setText("Insert Photo Title");


        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv1, tv1Params);
        layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle("Choose Album");
        // alertDialogBuilder.setMessage("Input Student ID");
        alertDialogBuilder.setCustomTitle(tv);

        albumArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumArrayList);
        alertDialogBuilder.setAdapter(albumArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = albumArrayAdapter.getItem(which);
                itemString = strName;
                imageTitle = et.getText().toString();
                saveFileToDrive();

            }
        });
    }
}
