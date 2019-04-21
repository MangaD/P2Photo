package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.widget.DataBufferAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ListUserAlbumActivity2 extends AppCompatActivity {

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_album2);

        new ListUserAlbumTask(ListUserAlbumActivity2.this).execute();
    }

    /**
     * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
     * https://www.androidstation.info/networkonmainthreadexception/
     * <p>
     * Template meaning:
     * 1st - argument type of 'doInBackground'
     * 2nd - argument type of 'onProgressUpdate'
     * 3rd - argument type of 'onPostExecute'
     */
    private static class ListUserAlbumTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<ListUserAlbumActivity2> activityReference;
        private ProgressDialog pd;

        private ListUserAlbumTask(ListUserAlbumActivity2 activity) {

            activityReference = new WeakReference<>(activity);

            // Create Progress dialog
            pd = new ProgressDialog(activity);
            pd.setMessage("Loading user's albums...");
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

            String msg = "Failed to contact the server.";

            try {
                ArrayList<String> list = conn.getUserAlbums();
                if (list == null) {
                    conn.disconnect();
                    Log.d("ListUserAlbumActivity2", msg);

                    activityReference.get().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        }
                    });

                    return false;
                } else {
                    activityReference.get().albumArrayList = list;

                    activityReference.get().albumListView = activityReference.get().findViewById(R.id.listViewAlbums);
                    activityReference.get().albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, activityReference.get().albumArrayList);
                    activityReference.get().albumListView.setAdapter(activityReference.get().albumArrayAdapter);

                    activityReference.get().albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String itemString = itemAtPosition.toString();

                        Intent viewAlbumIntent = new Intent(activityReference.get(), ViewAlbumActivity2.class);

                        viewAlbumIntent.putExtra("ViewAlbumName",itemString);

                        activityReference.get().startActivity(viewAlbumIntent);
                    });

                    return true;
                }
            } catch (IOException e) {
                conn.disconnect();
                Log.d("ListUserAlbumActivity2", msg);

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                });

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
            String successMsg = "Loaded user's albums list successfully.";
            String errorMsg = "Failed to load user's albums list.";
            Log.d("ListUserAlbumActivity2", successMsg);
            Toast.makeText(activityReference.get().getApplicationContext(), successMsg, Toast.LENGTH_LONG).show();
            if (success) {
                Log.d("ListUserAlbumActivity2", successMsg);
                for (String s : activityReference.get().albumArrayList) {
                    Log.d("ListUserAlbumActivity2", s);
                }
                Toast.makeText(activityReference.get().getApplicationContext(), successMsg, Toast.LENGTH_LONG).show();
            } else {
                Log.d("ListUserAlbumActivity2", errorMsg);
                Toast.makeText(activityReference.get().getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        }
    }
}
