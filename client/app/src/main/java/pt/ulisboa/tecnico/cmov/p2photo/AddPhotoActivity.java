package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

public class AddPhotoActivity extends AppCompatActivity {

    private static final String TAG = "add_photo";

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
    //private AlertDialog.Builder builder;

    AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        //puts the elements on a list on screen
        albumArrayList = globalVariable.getAlbumList();
        //albumListView = findViewById(R.id.listViewAlbums);
        //albumArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumArrayList);
        //albumListView.setAdapter(albumArrayAdapter);

        initButtons();
       // btnFindPicture.setVisibility(View.GONE);
        //btnTakePicture.setVisibility(View.GONE);

        /*albumListView.setVisibility(View.VISIBLE);
        albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
            Object itemAtPosition = adapter.getItemAtPosition(position);
            itemString = itemAtPosition.toString();
            Log.i(TAG, "ALBUM NAME: " + itemString);
            btnFindPicture.setVisibility(View.VISIBLE);
            btnTakePicture.setVisibility(View.VISIBLE);
            albumListView.setVisibility(View.GONE);
        });*/

        //createDialogOpts();

        dialogTest();

    }

    /** Create a new file and save it to Drive. */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;

        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        task -> createFileIntentSender(task.getResult(), image))
                .addOnFailureListener(
                        e -> Log.w(TAG, "Failed to create new contents.", e));
    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new photo in Drive.
     */
    private Task<DriveFile> createFileIntentSender(DriveContents driveContents, Bitmap image) {

        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        DriveId dId = globalVariable.findIndexAlbum("index"+itemString).getDriveid();
        appendContents(dId.asDriveFile());

        Log.i(TAG, "ALBUM NAME: " + itemString);

        DriveId dfId = globalVariable.findPhotoAlbum(itemString).getDriveid();

        return createImageInAlbum(dfId.asDriveFolder(), driveContents, image);
    }

    /*private Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {
        Log.i(TAG, "New contents created.");
        // Get an output stream for the contents.
        OutputStream outputStream = driveContents.getOutputStream();
        // Write the bitmap data from it.
        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        try {
            outputStream.write(bitmapStream.toByteArray());
        } catch (IOException e) {
            Log.w(TAG, "Unable to write file contents.", e);
        }

        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg")
                        .setTitle("Android Photo.png")
                        .build();
        // Set up options to configure and display the create file activity.
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        task -> {
                            startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                            return null;
                        });
    }*/

    private Task<DriveFile> createImageInAlbum(final DriveFolder parent, DriveContents driveContents, Bitmap image) {
        return mDriveResourceClient
                .createContents()
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    Log.i(TAG, "New contents created.");
                    // Get an output stream for the contents.
                    OutputStream outputStream = driveContents.getOutputStream();
                    // Write the bitmap data from it.
                    ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                    try {
                        outputStream.write(bitmapStream.toByteArray());
                    } catch (IOException e) {
                        Log.w(TAG, "Unable to write file contents.", e);
                    }

                    // Create the initial metadata - MIME type and title.
                    // Note that the user will be able to change the title later.
                    MetadataChangeSet metadataChangeSet =
                            new MetadataChangeSet.Builder()
                                    .setMimeType("image/jpeg")
                                    .setTitle(imageTitle+".png"/*"Android Photo.png"*/)
                                    .build();

                    return mDriveResourceClient.createFile(parent, metadataChangeSet, contents);
                })
                .addOnSuccessListener(this,
                        driveFile -> showMessage("Image created " +
                                driveFile.getDriveId().encodeToString()))
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to create file", e);
                    showMessage("Image create error");
                });
    }

    private void appendContents(DriveFile file) {
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
                out.write("Hello world\n".getBytes());
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
            case REQUEST_CODE_FIND_IMAGE:
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
            case REQUEST_CODE_CAPTURE_IMAGE:
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
            case REQUEST_CODE_CREATOR:
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

    void initButtons(){
        /*
         * opens the activity responsible for CREATING ALBUMS
         */
        btnFindPicture = findViewById(R.id.find_picture);
        btnFindPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start getContent
                //Intent cam_ImagesIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //cam_ImagesIntent.setType("image/*");
                //startActivityForResult(cam_ImagesIntent, REQUEST_CODE_FIND_IMAGE);

                startActivityForResult(
                        new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_CODE_FIND_IMAGE);
            }
        });
        /*
         * add photo to album
         * */
        btnTakePicture = findViewById(R.id.take_picture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start camera.
                startActivityForResult(
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
            }
        });
    }

    /*void createDialogOpts(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert Photo Title");


        final TextView tvChoosenAlbum = new TextView(this);
        tvChoosenAlbum.setText("Album: ");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setAdapter(albumArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = albumArrayAdapter.getItem(which).getName();
                AlertDialog.Builder builderInner = new AlertDialog.Builder(builder.getContext());
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();

               tvChoosenAlbum.setText("Album: " + strName);

                //imageTitle = input.getText().toString();
                //saveFileToDrive();

            }
        });

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageTitle = input.getText().toString();
                    saveFileToDrive();
                }
        });
    }*/

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    void dialogTest() {
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
        alertDialogBuilder.setAdapter(albumArrayAdapter,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = albumArrayAdapter.getItem(which).getName();
                itemString = strName;
                imageTitle = et.getText().toString();
                saveFileToDrive();

            }
        });




/*
        // alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Setting Positive "OK" Button
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });*/

    }
}
