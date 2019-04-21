package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

public class ViewPhotoActivity extends AppCompatActivity  {

    ImageView testImageView;

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private static final String TAG = "view_photo";
    private String photo_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        testImageView = this.findViewById(R.id.testImageView);

        photo_url = "https://gssc.esa.int/navipedia/images/a/a9/Example.jpg"; //example URL to be CHANGED

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
