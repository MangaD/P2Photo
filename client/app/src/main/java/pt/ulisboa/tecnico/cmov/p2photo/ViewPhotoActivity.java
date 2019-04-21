package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
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

       /* private Task<Bitmap> getImageFromUrl (String url_name){
            try {
                URL url = new URL(url_name);
                Log.i(TAG, "AFTER URL.");
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Log.i(TAG, "AFTER bitmap.");

                testImageView.setImageBitmap(bmp);
                Log.i(TAG, "AFTER image view.");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i(TAG, "bad url.");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "bad io.");
            }catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "random exception.");
            }
        return null;
    }*/

    /**
     * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
     * https://www.androidstation.info/networkonmainthreadexception/
     * <p>
     * Template meaning:
     * 1st - argument type of 'doInBackground'
     * 2nd - argument type of 'onProgressUpdate'
     * 3rd - argument type of 'onPostExecute'
     */
    private static class ObtainPhotoTask extends AsyncTask<Void, Void, Bitmap> {

        private WeakReference<ViewPhotoActivity> activityReference;
        private ProgressDialog pd;
        private String imageURL;
        private ImageView testImageView;

        private ObtainPhotoTask(ViewPhotoActivity activity) {

            activityReference = new WeakReference<>(activity);
            imageURL = activity.photo_url;
            testImageView = activity.testImageView;

            // Create Progress dialog
            pd = new ProgressDialog(activity);
            pd.setMessage("Downloading image...");
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
        protected Bitmap doInBackground(Void... values) {
            try {
                URL url = new URL(imageURL);
                InputStream stream = url.openConnection().getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(stream);
                //testImageView.setImageBitmap(bmp);
                pd.dismiss();
                return bmp;
            }catch (Exception e){
                //
            }
            pd.dismiss();
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            super.onPreExecute();

            testImageView.setImageBitmap(bmp);


        }
    }
}
