package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewPhotoActivity;

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

    public static final String TAG = "ViewAlbumTask";

    private WeakReference<ViewAlbumActivity> activityReference;
    private ProgressDialog pd;
    GlobalClass context;

    String albumName;

    private ArrayList<String> indexURLs;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    public ViewAlbumTask(GlobalClass ctx, ViewAlbumActivity activity, String albumName) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        this.albumName = albumName;
        this.albumArrayList = new ArrayList<>();

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
        ServerConnection conn = context.getServerConnection();

        try {
            Log.d(TAG, "Getting indexes for album: " + this.albumName);
            String encKeyBase64 = conn.getAlbumKey(this.albumName);
            ArrayList<String> list = conn.getAlbumIndexes(this.albumName);
            if (list == null || encKeyBase64 == null) {
                conn.disconnect();
                Log.d(TAG, context.getString(R.string.server_contact_fail));
                showMessage(context.getString(R.string.server_contact_fail));
                return false;
            } else {
                this.indexURLs = list;

                Log.d(TAG, "List size: " + list.size());

                // TODO decrypt urls in index
                for (String entry : this.indexURLs) {
                    Log.d(TAG, entry);
                    URL index = new URL(entry);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(index.openStream()));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        Log.d(TAG, inputLine);
                        this.albumArrayList.add(inputLine);
                    }
                    in.close();
                }

                this.activityReference.get().runOnUiThread(() -> {
                    this.albumListView = activityReference.get().findViewById(R.id.listViewPhotoItems);
                    this.albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, this.albumArrayList);
                    this.albumListView.setAdapter(this.albumArrayAdapter);

                    this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String itemString = itemAtPosition.toString();

                        Intent viewPhotoIntent = new Intent(activityReference.get(), ViewPhotoActivity.class);

                        viewPhotoIntent.putExtra("ViewPhotoName", itemString);

                        activityReference.get().startActivity(viewPhotoIntent);
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
        if (success) {
            Log.d(TAG, context.getString(R.string.load_album_photos_success));
            for (String entry : this.indexURLs) {
                Log.d(TAG, entry);
            }
        } else {
            Log.d(TAG, context.getString(R.string.load_user_album_fail));
            Toast.makeText(activityReference.get().getApplicationContext(), context.getString(R.string.load_album_photos_fail), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        );
    }
}