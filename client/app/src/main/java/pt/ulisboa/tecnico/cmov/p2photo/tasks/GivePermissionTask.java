package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.GivePermissionActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.LoggedInActivity;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class GivePermissionTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "GivePermissionTask";

    private WeakReference<GivePermissionActivity> activityReference;
    private ProgressDialog pd;
    private GlobalClass ctx;

    String userName;
    String albumName;

    public GivePermissionTask(GivePermissionActivity activity, String userName, String albumName) {

        activityReference = new WeakReference<>(activity);

        ctx = (GlobalClass) activity.getApplicationContext();

        this.userName = userName;
        this.albumName = albumName;

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage("Giving permission to album '" + albumName + "' to user '" + userName + "'");
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

        ServerConnection conn = ctx.getServerConnection();

        try {
            String msg = conn.givePermission(userName, albumName, "");
            return msg;
        } catch (IOException e) {
            conn.disconnect();
            String msg = ctx.getString(R.string.server_connect_fail);
            Log.d(TAG, msg);

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
            );
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

        Log.d(TAG, msg);
        Toast.makeText(activityReference.get().getApplicationContext(), msg, Toast.LENGTH_LONG).show();

        Intent mainIntent = new Intent(activityReference.get(), LoggedInActivity.class);
        activityReference.get().startActivity(mainIntent);
    }
}