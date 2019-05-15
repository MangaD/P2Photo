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
            HashMap<Integer, String> list = conn.getUserAlbums();
            if (list == null) {
                conn.disconnect();
                Log.d("AddUserToAlbumActivity", context.getString(R.string.server_contact_fail));

                activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
                );

                return false;
            } else {
                this.albumArrayList = new ArrayList<>(list.values());

                this.activityReference.get().runOnUiThread(() -> {
                    this.albumListView = activityReference.get().findViewById(R.id.listViewAlbumsAddUser);
                    this.albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, this.albumArrayList);
                    this.albumListView.setAdapter(this.albumArrayAdapter);

                    this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String itemString = itemAtPosition.toString();

                        Intent viewAlbumIntent = new Intent(activityReference.get(), FindUserActivity.class);

                        viewAlbumIntent.putExtra("ViewAlbumName", itemString);

                        activityReference.get().startActivity(viewAlbumIntent);
                    });
                });

                return true;
            }
        } catch (IOException e) {
            conn.disconnect();
            Log.d("AddUserToAlbumActivity", context.getString(R.string.server_contact_fail));

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
            );

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
        Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_user_album_success), Toast.LENGTH_LONG).show();
        if (success) {
            Log.d("AddUserToAlbumActivity", context.getString(R.string.load_user_album_success));
            for (String entry : this.albumArrayList) {
                Log.d("AddUserToAlbumActivity", entry);
            }
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_user_album_success), Toast.LENGTH_LONG).show();
        } else {
            Log.d("AddUserToAlbumActivity", context.getString(R.string.load_user_album_fail));
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_user_album_fail), Toast.LENGTH_LONG).show();
        }
    }
}