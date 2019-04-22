package pt.ulisboa.tecnico.cmov.p2photo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.AddUserToAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.FindUserActivity;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;

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

    private WeakReference<AddUserToAlbumActivity> activityReference;
    private ProgressDialog pd;
    Context ctx;

    public AddUserToAlbumTask(AddUserToAlbumActivity activity) {

        activityReference = new WeakReference<>(activity);
        ctx = activity.getApplicationContext();

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
            ArrayList<String> list = conn.getUserAlbums();
            if (list == null) {
                conn.disconnect();
                Log.d("ListUserAlbumActivity2", ctx.getString(R.string.server_contact_fail));

                activityReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(context, ctx.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show();
                    }
                });

                return false;
            } else {
                activityReference.get().setAlbumArrayList(list);

                activityReference.get().setAlbumListView(activityReference.get().findViewById(R.id.listViewAlbums));
                activityReference.get().setAlbumArrayAdapter(new ArrayAdapter<>(activityReference.get(),
                        android.R.layout.simple_list_item_1, activityReference.get().getAlbumArrayList()));
                activityReference.get().getAlbumListView().setAdapter(activityReference.get().getAlbumArrayAdapter());

                activityReference.get().getAlbumListView().setOnItemClickListener((adapter, view, position, arg) -> {
                    Object itemAtPosition = adapter.getItemAtPosition(position);
                    String itemString = itemAtPosition.toString();

                    Intent viewAlbumIntent = new Intent(activityReference.get(), FindUserActivity.class);

                    viewAlbumIntent.putExtra("ViewAlbumName",itemString);

                    activityReference.get().startActivity(viewAlbumIntent);
                });

                return true;
            }
        } catch (IOException e) {
            conn.disconnect();
            Log.d("ListUserAlbumActivity2", ctx.getString(R.string.server_contact_fail));

            activityReference.get().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, ctx.getString(R.string.server_contact_fail), Toast.LENGTH_LONG).show();
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
        //String successMsg = "Loaded user's albums list successfully.";
        //String errorMsg = "Failed to load user's albums list.";
        Log.d("ListUserAlbumActivity2", ctx.getString(R.string.load_user_album_success));
        Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_album_success), Toast.LENGTH_LONG).show();
        if (success) {
            Log.d("ListUserAlbumActivity2", ctx.getString(R.string.load_user_album_success));
            for (String s : activityReference.get().getAlbumArrayList()) {
                Log.d("ListUserAlbumActivity2", s);
            }
            Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_album_success), Toast.LENGTH_LONG).show();
        } else {
            Log.d("ListUserAlbumActivity2", ctx.getString(R.string.load_user_album_fail));
            Toast.makeText(activityReference.get().getApplicationContext(), ctx.getString(R.string.load_user_album_fail), Toast.LENGTH_LONG).show();
        }
    }
}