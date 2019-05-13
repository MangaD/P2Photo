package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
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
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddPhotoActivity;

import static android.app.Activity.RESULT_OK;

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

    private WeakReference<AddPhotoActivity> activityReference;
    private ProgressDialog pd;
    private GlobalClass context;

    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_FIND_IMAGE = 4;

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Bitmap mBitmapToSave;

    private ListView albumListView;
    private ArrayList<GlobalClass.PhotoAlbum> albumArrayList;
    private ArrayAdapter<GlobalClass.PhotoAlbum> albumArrayAdapter;

    private String itemString = "null";
    Button btnTakePicture;
    Button btnFindPicture;
    private String imageTitle = "";


    AlertDialog.Builder alertDialogBuilder;


    public AddPhotoTask(GlobalClass ctx, AddPhotoActivity activity) {

        activityReference = new WeakReference<>(activity);

        this.context = ctx;

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

        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = this.context.getmDriveClient();
        this.mDriveResourceClient = this.context.getmDriveResourceClient();

        //puts the elements on a list on screen
        albumArrayList = this.context.getAlbumList();

        /*
         * opens the activity responsible for CREATING ALBUMS
         */
        btnFindPicture = activityReference.get().findViewById(R.id.find_picture);
        btnFindPicture.setOnClickListener((View view) ->
                //Start getContent
                activityReference.get().startActivityForResult(
                        new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_CODE_FIND_IMAGE)
        );
        /*
         * add photo to album
         * */
        btnTakePicture = activityReference.get().findViewById(R.id.take_picture);
        btnTakePicture.setOnClickListener((View view) ->
                // Start camera.
                activityReference.get().startActivityForResult(
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE)
        );

        createDialogOpts();

        // TODO
        return "";
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(String msg) {
        pd.dismiss();
        Log.d("AddPhotoTask", msg);
        Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        activityReference.get().finish();
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i("AddPhotoTask", "Creating new contents.");
        final Bitmap image = mBitmapToSave;

        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        task ->
                                createFileIntentSender(task.getResult(), image)
                )
                .addOnFailureListener(
                        e -> Log.w("AddPhotoTask", "Failed to create new contents.", e));

    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new photo in Drive.
     */
    private Task<DriveFile> createFileIntentSender(DriveContents driveContents, Bitmap image) {

        //DriveId diId = context.findIndexAlbum("index"+itemString).getDriveID();
        //appendContents(diId.asDriveFile());

        Log.i("AddPhotoTask", "ALBUM NAME: " + itemString);

        DriveId dfId = context.findPhotoAlbum(itemString).getDriveid();

        return createImageInAlbum(dfId.asDriveFolder(), driveContents, image);
    }

    private Task<DriveFile> createImageInAlbum(final DriveFolder parent, DriveContents driveContents, Bitmap image) {
        // [START drive_android_create_file]
        final Task<DriveFolder> rootFolderTask = mDriveResourceClient.getRootFolder();
        final Task<DriveContents> createContentsTask = mDriveResourceClient.createContents();
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

                    return mDriveResourceClient.createFile(parent, changeSet, contents);
                })
                .addOnSuccessListener(activityReference.get(),
                        driveFile -> {

                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            GlobalClass.IndexAlbum diId = context.findIndexAlbum("index" + itemString);
                            DriveFile indexFile = diId.getDriveid().asDriveFile();
                            Task<Metadata> queryTask = mDriveResourceClient.getMetadata(driveFile);
                            queryTask
                                    .addOnSuccessListener(activityReference.get(),
                                            Metadata -> {
                                                String link2 = queryTask.getResult().getEmbedLink();
                                                Log.i("LINK", "Success getting URL Embeded " + link2);
                                                showMessage("Success getting URL " + link2);
                                                appendContents(indexFile, link2);
                                            })
                                    .addOnFailureListener(activityReference.get(), e -> {
                                        Log.i("LINK", "Error getting URL");
                                        showMessage("Error getting URL");
                                        activityReference.get().finish();
                                    });

                            showMessage("Image created " +
                                    driveFile.getDriveId().encodeToString());
                            activityReference.get().finish();
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e("AddPhotoTask", "Unable to create file", e);
                    activityReference.get().finish();
                });
        // [END drive_android_create_file]
        return null;
    }

    private void appendContents(DriveFile file, String link) {

        // [START drive_android_open_for_append]
        Task<DriveContents> openTask =
                mDriveResourceClient.openFile(file, DriveFile.MODE_READ_WRITE);
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
                    mDriveResourceClient.commitContents(driveContents, changeSet);
            // [END drive_android_commit_contents_with_metadata]
            return commitTask;
        })
                .addOnSuccessListener(activityReference.get(),
                        aVoid -> {
                            showMessage("Content updated");
                            activityReference.get().finish();
                        })
                .addOnFailureListener(activityReference.get(), e -> {
                    Log.e("AddPhotoTask", "Unable to update contents", e);
                    showMessage("Content update failed");
                    activityReference.get().finish();
                });
        // [END drive_android_append_contents]
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_FIND_IMAGE:
                Log.i("AddPhotoTask", "find image request code");
                // Called after a photo has been taken.
                if (resultCode == RESULT_OK) {
                    Log.i("AddPhotoTask", "Image finded successfully.");
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
            case REQUEST_CODE_CAPTURE_IMAGE:
                Log.i("AddPhotoTask", "capture image request code");
                // Called after a photo has been taken.
                if (resultCode == RESULT_OK) {
                    Log.i("AddPhotoTask", "Image captured successfully.");
                    // Store the image data as a bitmap for writing later.
                    mBitmapToSave = (Bitmap) data.getExtras().get("data");
                    Log.i("AddPhotoTask", "mBitmapToSave: " + mBitmapToSave.toString());
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
            case REQUEST_CODE_CREATOR:
                Log.i("AddPhotoTask", "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i("AddPhotoTask", "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    //startActivityForResult(
                    //      new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
        }
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        );
    }

    void createDialogOpts() {
        alertDialogBuilder = new AlertDialog.Builder(context);

        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        TextView tv = new TextView(context);
        tv.setText("Choose Album and Insert Photo Title");
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        EditText et = new EditText(context);
        imageTitle = et.getText().toString();
        TextView tv1 = new TextView(context);
        tv1.setText("Insert Photo Title");


        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv1, tv1Params);
        layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle("Choose Album");
        // alertDialogBuilder.setMessage("Input Student ID");
        alertDialogBuilder.setCustomTitle(tv);

        albumArrayAdapter = new ArrayAdapter<>(activityReference.get(), android.R.layout.simple_list_item_1, albumArrayList);
        alertDialogBuilder.setAdapter(albumArrayAdapter, (DialogInterface dialog, int which) -> {
            String strName = albumArrayAdapter.getItem(which).getName();
            itemString = strName;
            imageTitle = et.getText().toString();
            saveFileToDrive();
        });
    }

}