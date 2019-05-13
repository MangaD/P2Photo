package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewAlbumActivity2;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class ViewAlbumTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<ViewAlbumActivity2> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    String albumName;

    private ArrayList<String> indexURLs;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    public ViewAlbumTask(GlobalClass ctx, ViewAlbumActivity2 activity, String albumName) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        this.albumName = albumName;

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(ctx.getString(R.string.load_album_photos));
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

        GlobalClass context = (GlobalClass) activityReference.get().getApplicationContext();
        ServerConnection conn = context.getConnection();

        try {
            Log.d("ViewAlbumTask", "Getting indexes for album: " + this.albumName);
            ArrayList<String> list = conn.getAlbumIndexes(this.albumName);
            if (list == null) {
                conn.disconnect();
                Log.d("ViewAlbumTask", context.getString(R.string.server_contact_fail));

                activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
                );

                return false;
            } else {
                this.indexURLs = list;

                Log.d("ViewAlbumTask", "List size: " + list.size());
                for (String entry : this.indexURLs) {
                    Log.d("ViewAlbumTask", entry);
                }/*

                this.activityReference.get().runOnUiThread(() -> {
                    this.albumListView = activityReference.get().findViewById(R.id.listViewAlbums);
                    this.albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, this.albumArrayList);
                    this.albumListView.setAdapter(this.albumArrayAdapter);


                    this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String itemString = itemAtPosition.toString();

                        Intent viewAlbumIntent = new Intent(activityReference.get(), ViewAlbumActivity2.class);

                        viewAlbumIntent.putExtra("ViewAlbumName",itemString);

                        activityReference.get().startActivity(viewAlbumIntent);
                    });
                });*/

                return true;
            }
        } catch (IOException e) {
            conn.disconnect();
            Log.d("ViewAlbumTask", context.getString(R.string.server_contact_fail));

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
        if (success) {
            Log.d("ViewAlbumTask", context.getString(R.string.load_album_photos_success));
            for (String entry : this.indexURLs) {
                Log.d("ViewAlbumTask", entry);
            }
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_album_photos_success), Toast.LENGTH_LONG).show();
        } else {
            Log.d("ViewAlbumTask", context.getString(R.string.load_user_album_fail));
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_album_photos_fail), Toast.LENGTH_LONG).show();
        }
    }
}