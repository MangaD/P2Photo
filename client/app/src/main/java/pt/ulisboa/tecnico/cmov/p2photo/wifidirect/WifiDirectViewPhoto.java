package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewPhotoActivity;

public class WifiDirectViewPhoto {
    private GlobalClass context;
    private WeakReference<ViewPhotoActivity> activityReference;
    private String photoUrl;
    private Bitmap imageBitmap;
    private ImageView testImageView;
    public WifiDirectViewPhoto(GlobalClass ctx, ViewPhotoActivity viewPhotoActivity, String photo_url) {

        context=ctx;
        activityReference = new WeakReference<>(viewPhotoActivity);
        photoUrl=photo_url;
        testImageView = activityReference.get().testImageView;
        getPhoto();
        showPhoto();
        
    }

    private void showPhoto() {
        testImageView.setImageBitmap(imageBitmap);
    }

    private void getPhoto() {
        try {
            Toast.makeText(context,photoUrl,Toast.LENGTH_SHORT).show();
            FileInputStream is = activityReference.get().openFileInput(photoUrl);
            imageBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
