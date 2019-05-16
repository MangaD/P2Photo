package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.InputStream;

import pt.ulisboa.tecnico.cmov.p2photo.DriveConnection;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.AddPhotoTask;

public class AddPhotoActivity extends AppCompatActivity {

    public static final String TAG = "AddPhotoActivity";

    private GlobalClass context;

    public AlertDialog.Builder alertDialogBuilder;
    public Bitmap mBitmapToSave;

    Button btnTakePicture;
    Button btnFindPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        this.context = (GlobalClass) getApplicationContext();

        new AddPhotoTask((GlobalClass) getApplicationContext(), this).execute();
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


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case DriveConnection.REQUEST_CODE_FIND_IMAGE:
                Log.i(TAG, "find image request code");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Image found successfully.");
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

}
