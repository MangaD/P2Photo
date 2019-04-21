package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FindUserActivity extends AppCompatActivity {

    private static final String TAG = "find_user_album";
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private ListView userListView;
    private ArrayList<String> userArrayList;
    private ArrayAdapter<String> userArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        new FindUsersTask(FindUserActivity.this).execute();
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
    private static class FindUsersTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<FindUserActivity> activityReference;
        private ProgressDialog pd;
        private Context ctx;

        private FindUsersTask(FindUserActivity activity) {

            activityReference = new WeakReference<>(activity);

            ctx = activity.getApplicationContext();

            // Create Progress dialog
            pd = new ProgressDialog(activity);
            pd.setMessage(ctx.getString(R.string.load_user_list));
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
                ArrayList<String> list = conn.getUsers();
                if (list == null) {
                    conn.disconnect();
                    Log.d("FindUserActivity", ctx.getString(R.string.server_connection_fail));

                    activityReference.get().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, ctx.getString(R.string.server_connection_fail), Toast.LENGTH_LONG).show();
                        }
                    });

                    return false;
                } else {
                    activityReference.get().userArrayList = list;

                    activityReference.get().userListView = activityReference.get().findViewById(R.id.listUsers);
                    activityReference.get().userArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, activityReference.get().userArrayList);
                    activityReference.get().userListView.setAdapter(activityReference.get().userArrayAdapter);

                    activityReference.get().userListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String itemString = itemAtPosition.toString();

                        Intent viewAlbumIntent = new Intent(activityReference.get(), AddUserToAlbumActivity.class);

                        activityReference.get().startActivity(viewAlbumIntent);
                    });

                    return true;
                }
            } catch (IOException e) {
                conn.disconnect();
                Log.d("FindUserActivity", ctx.getString(R.string.server_connection_fail));

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, ctx.getString(R.string.server_connection_fail), Toast.LENGTH_LONG).show();
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


            Log.d("FindUserActivity", ctx.getString(R.string.load_user_list_success));
            Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_list_success), Toast.LENGTH_LONG).show();
            if (success) {
                Log.d("FindUserActivity", ctx.getString(R.string.load_user_list_success));
                for (String s : activityReference.get().userArrayList) {
                    Log.d("FindUserActivity", s);
                }
                Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_list_success), Toast.LENGTH_LONG).show();
            } else {
                Log.d("FindUserActivity", ctx.getString(R.string.load_user_list_fail));
                Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_list_fail), Toast.LENGTH_LONG).show();
            }
        }
    }

}
