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
import java.util.Set;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListUserAlbumActivity2;
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
public class ListUserAlbumTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<ListUserAlbumActivity2> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    public ListUserAlbumTask(GlobalClass ctx, ListUserAlbumActivity2 activity) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;

        // Create Progress dialog
        pd = new ProgressDialog(activity);
        pd.setMessage(ctx.getString(R.string.load_user_album));
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

        //String msg = "Failed to contact the server.";

        try {
            HashMap<Integer, String> list = conn.getUserAlbums();
            if (list == null) {
                conn.disconnect();
                Log.d("ListUserAlbumTask", context.getString(R.string.server_contact_fail));

                activityReference.get().runOnUiThread(() ->
                    Toast.makeText(context, context.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show()
                );

                return false;
            } else {
                this.albumArrayList = new ArrayList<>(list.values());

                Log.d("ListUserAlbumTask", "List size: " + list.size());
                for (String entry : this.albumArrayList) {
                    Log.d("ListUserAlbumTask", entry);
                }

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
                });

                return true;
            }
        } catch (IOException e) {
            conn.disconnect();
            Log.d("ListUserAlbumTask", context.getString(R.string.server_contact_fail));

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
            Log.d("ListUserAlbumTask", context.getString(R.string.load_user_album_success));
            for (String entry : this.albumArrayList) {
                Log.d("ListUserAlbumTask", entry);
            }
        } else {
            Log.d("ListUserAlbumTask", context.getString(R.string.load_user_album_fail));
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_user_album_fail), Toast.LENGTH_LONG).show();
        }
    }
}