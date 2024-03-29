package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddUserToAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.FindUserActivity;


/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class AddUserToAlbumTask extends AsyncTask<Void, Void, Boolean> {

    public static final String TAG = "AddUserToAlbumTask";
    
    private WeakReference<AddUserToAlbumActivity> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    private HashMap<Integer, String> albumsMap;

    public AddUserToAlbumTask(GlobalClass ctx, AddUserToAlbumActivity activity) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(context.getString(R.string.load_user_album));
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
    protected Boolean doInBackground(Void... values) {

        ServerConnection conn = context.getServerConnection();

        //String msg = "Failed to contact the server.";

        try {
            albumsMap = conn.getUsersOwnedAlbums();
            if (albumsMap == null) {
                conn.disconnect();
                Log.d(TAG, context.getString(R.string.server_contact_fail));
                showMessage(context.getString(R.string.server_contact_fail));
                return false;
            } else {
                this.albumArrayList = new ArrayList<>(albumsMap.values());

                this.activityReference.get().runOnUiThread(() -> {
                    this.albumListView = activityReference.get().findViewById(R.id.listViewAlbumsAddUser);
                    this.albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, this.albumArrayList);
                    this.albumListView.setAdapter(this.albumArrayAdapter);

                    this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String albumName = itemAtPosition.toString();

                        Intent findUserIntent = new Intent(activityReference.get(), FindUserActivity.class);

                        findUserIntent.putExtra("FindActivityAlbumName", albumName);

                        activityReference.get().startActivity(findUserIntent);
                    });
                });

                return true;
            }
        } catch (IOException e) {
            conn.disconnect();
            Log.d(TAG, context.getString(R.string.server_contact_fail));

            showMessage(context.getString(R.string.server_contact_fail));

            return false;
        }
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(Boolean success) {
        pd.dismiss();
        //String successMsg = "Loaded user's albums list successfully.";
        //String errorMsg = "Failed to load user's albums list.";
        Log.d("AddUserToAlbumActivity", context.getString(R.string.load_user_album_success));
        if (success) {
            Log.d("AddUserToAlbumActivity", context.getString(R.string.load_user_album_success));
            for (String entry : this.albumArrayList) {
                Log.d("AddUserToAlbumActivity", entry);
            }
        } else {
            Log.d("AddUserToAlbumActivity", context.getString(R.string.load_user_album_fail));
            showMessage(context.getString(R.string.load_user_album_fail));
        }
    }

    private void showMessage(String msg) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        );
    }
}