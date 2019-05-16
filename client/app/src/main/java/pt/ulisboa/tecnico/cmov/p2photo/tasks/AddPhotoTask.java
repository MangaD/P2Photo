package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddPhotoActivity;

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

    public static final String TAG = "AddPhotoTask";
    
    private WeakReference<AddPhotoActivity> activityReference;
    private GlobalClass context;

    private ArrayList<String> albumArrayList;

    public AddPhotoTask(GlobalClass ctx, AddPhotoActivity activity) {
        activityReference = new WeakReference<>(activity);
        this.context = ctx;
    }

    @Override
    protected String doInBackground(Void... values) {

        ServerConnection conn = context.getServerConnection();
        try {
            HashMap<Integer, String> hashMap = conn.getUsersAllowedAlbums();
            ArrayList<String> list = new ArrayList<>(hashMap.values());

            if (list == null) {
                conn.disconnect();
                Log.d(TAG, context.getString(R.string.server_contact_fail));

                activityReference.get().runOnUiThread(() ->
                        Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
                );
            } else {
                this.albumArrayList = list;
                Log.i(TAG, "List size: " + list.size());
                for (String s : list) {
                    Log.i(TAG, s);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "Failed to show albuns: " + e);
        }
        return "n";
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(String msg) {
        activityReference.get().initButtons();
        activityReference.get().createDialogOpts(albumArrayList);
    }
}