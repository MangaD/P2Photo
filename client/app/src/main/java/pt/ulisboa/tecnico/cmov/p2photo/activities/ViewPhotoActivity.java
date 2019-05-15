package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;


import java.lang.ref.WeakReference;
import java.net.URL;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class ViewPhotoActivity extends AppCompatActivity  {

    ImageView testImageView;

    private String photo_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        testImageView = this.findViewById(R.id.testImageView);

        photo_url = getIntent().getStringExtra("ViewPhotoName");

        new ObtainPhotoTask(ViewPhotoActivity.this).execute();

    }

    /**
     * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
     * https://www.androidstation.info/networkonmainthreadexception/
     * <p>
     * Template meaning:
     * 1st - argument type of 'doInBackground'
     * 2nd - argument type of 'onProgressUpdate' and 'publishProgress'
     * 3rd - argument type of 'onPostExecute'
     */
    private static class ObtainPhotoTask extends AsyncTask<Void, String, Bitmap> {

        private Context ctx;
        private WeakReference<ViewPhotoActivity> activityReference;
        private ProgressDialog pd;
        private String imageURL;
        private ImageView testImageView;

        private ObtainPhotoTask(ViewPhotoActivity activity) {

            activityReference = new WeakReference<>(activity);
            ctx = activity.getApplicationContext();
            imageURL = activity.photo_url;
            testImageView = activity.testImageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... values) {
            return loadImageFromNetwork(imageURL);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            testImageView.setImageBitmap((Bitmap) result);
        }

        private Bitmap loadImageFromNetwork(String url) {
            try {
                URL imageUrl = new URL(url);
                Bitmap image = BitmapFactory.decodeStream(imageUrl.openStream());
                if (image != null) {
                    publishProgress(ctx.getString(R.string.download_success));
                    Log.i("ViewPhotoActivity", ctx.getString(R.string.download_success));
                } else {
                    publishProgress(ctx.getString(R.string.download_failed_stream));
                    Log.i("ViewPhotoActivity", ctx.getString(R.string.download_failed_stream));
                }
                return image;
            } catch (Exception e) {
                publishProgress(ctx.getString(R.string.download_failed));
                Log.i("ViewPhotoActivity", ctx.getString(R.string.download_failed));
                e.printStackTrace();
                return null;
            }
        }
    }
}
