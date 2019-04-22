package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.CreateAlbumActivity;

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

    private WeakReference<CreateAlbumActivity> activityReference;
    private ProgressDialog pd;

    private String albumName;

    private GlobalClass context;

    public CreateAlbumTask(GlobalClass ctx, CreateAlbumActivity activity) {

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

        ServerConnection conn = context.getConnection();

        EditText albumNameEdit = activityReference.get().findViewById(R.id.albumName);
        albumName = albumNameEdit.getText().toString();

        if (albumName.isEmpty()) {
            conn.disconnect();
            String msg = context.getString(R.string.album_empty_name);
            Log.d("CreateAlbumActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

            return msg;
        }

        Log.d("CreateAlbumActivity", "Album name: " + albumName);

        try {
            // Add album to server
            String msg = conn.createAlbum(albumName);

            // Add album to drive
            if (msg.equals(context.getString(R.string.album_create_success))) {
                //globalVariable.addAlbumToAlbumList(albumName); //saves the name of the album locally
                if (!activityReference.get().albumNameExists(albumName)) {
                    activityReference.get().createFolder(albumName);
                }
            }

            // Add index of album to server
            msg = conn.setAlbumIndex(albumName, activityReference.get().getIndexURL());

            return msg;
        } catch (IOException e) {
            conn.disconnect();
            String msg = context.getString(R.string.server_connect_fail);
            Log.d("CreateAlbumActivity", msg);

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });

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
        Log.d("CreateAlbumActivity", msg);
        Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}