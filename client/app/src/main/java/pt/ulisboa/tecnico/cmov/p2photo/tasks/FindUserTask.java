package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Context;
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
public class FindUserTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<FindUserActivity> activityReference;
    private ProgressDialog pd;
    private GlobalClass ctx;

    String albumName;

    private ListView userListView;
    private ArrayList<String> userArrayList;
    private ArrayAdapter<String> userArrayAdapter;

    public FindUserTask(FindUserActivity activity, String albumName) {

        activityReference = new WeakReference<>(activity);

        ctx = (GlobalClass) activity.getApplicationContext();

        this.albumName = albumName;

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

        ServerConnection conn = ctx.getConnection();

        try {
            ArrayList<String> list = conn.getUsers();
            if (list == null) {
                conn.disconnect();
                Log.d("FindUserActivity", ctx.getString(R.string.server_connect_fail));

                activityReference.get().runOnUiThread(() ->
                    Toast.makeText(ctx, ctx.getString(R.string.server_connect_fail), Toast.LENGTH_LONG).show()
                );

                return false;
            } else {
                this.userArrayList = list;

                this.activityReference.get().runOnUiThread(() -> {
                    this.userListView = activityReference.get().findViewById(R.id.listUsers);
                    this.userArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, this.userArrayList);
                    this.userListView.setAdapter(this.userArrayAdapter);

                    this.userListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String userName = itemAtPosition.toString();
                        try {
                            conn.givePermission(userName, albumName, "");
                        } catch (IOException e) {}
                    });
                });

                return true;
            }
        } catch (IOException e) {
            conn.disconnect();
            Log.d("FindUserActivity", ctx.getString(R.string.server_connect_fail));

            activityReference.get().runOnUiThread(() ->
                Toast.makeText(ctx, ctx.getString(R.string.server_connect_fail), Toast.LENGTH_LONG).show()
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


        Log.d("FindUserActivity", ctx.getString(R.string.load_user_list_success));
        Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_list_success), Toast.LENGTH_LONG).show();
        if (success) {
            Log.d("FindUserActivity", ctx.getString(R.string.load_user_list_success));
            for (String s : this.userArrayList) {
                Log.d("FindUserActivity", s);
            }
            Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_list_success), Toast.LENGTH_LONG).show();
        } else {
            Log.d("FindUserActivity", ctx.getString(R.string.load_user_list_fail));
            Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_list_fail), Toast.LENGTH_LONG).show();
        }
    }
}