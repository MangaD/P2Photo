package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddPhotoActivity;

public class AddPhotoTaskPart3 extends AsyncTask<Void, Void, Boolean> {

    public static final String TAG = "AddPhotoTaskPart3";

    private WeakReference<AddPhotoActivity> activityReference;
    private GlobalClass context;

    private String albumName;
    private String encKeyBase64;
    private String indexURL;

    public AddPhotoTaskPart3(GlobalClass ctx, AddPhotoActivity activity, String albumName, String indexURL, String encKeyBase64) {
        activityReference = new WeakReference<>(activity);
        this.context = ctx;

        this.albumName = albumName;
        this.encKeyBase64 = encKeyBase64;
        this.indexURL = indexURL;
    }

    @Override
    protected Boolean doInBackground(Void... values) {

        ServerConnection conn = context.getServerConnection();

        try {
            String msg = conn.setAlbumIndex(albumName, indexURL, this.encKeyBase64);
            Log.d(TAG, "Album index url set to '" + indexURL + "' in server?\n" + msg);
        } catch (IOException e) {
            Log.i(TAG, "Failed to add URL Index: " + e);
            showMessage("Failed to add URL Index: " + e);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) { }

    private void showMessage(String msg) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        );
    }
}
