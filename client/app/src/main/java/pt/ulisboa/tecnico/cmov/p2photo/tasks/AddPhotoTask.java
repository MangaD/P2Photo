package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.AddPhotoActivity;

/**
 * Uses AsyncTask to create a task away from the main UI thread (to avoid NetworkOnMainThreadException).
 * https://www.androidstation.info/networkonmainthreadexception/
 * <p>
 * Template meaning:
 * 1st - argument type of 'doInBackground'
 * 2nd - argument type of 'onProgressUpdate'
 * 3rd - argument type of 'onPostExecute'
 */
public class AddPhotoTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "AddPhotoTask";
    
    private WeakReference<AddPhotoActivity> activityReference;
    private GlobalClass context;
    private ProgressDialog pd;

    private ArrayList<String> albumArrayList;

    private ArrayAdapter<String> albumArrayAdapter;

    private String albumName = null;
    private String imageTitle = "";

    public AddPhotoTask(GlobalClass ctx, AddPhotoActivity activity) {
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
    protected String doInBackground(Void... values) {

        ServerConnection conn = context.getServerConnection();
        try {
            /*
             * Get user's allowed albums
             */
            HashMap<Integer, String> hashMap = conn.getUsersAllowedAlbums();

            if (hashMap == null) {
                conn.disconnect();
                Log.d(TAG, context.getString(R.string.server_contact_fail));

                showMessage(context.getString(R.string.server_contact_fail));
            } else {
                this.albumArrayList = new ArrayList<>(hashMap.values());
                Log.i(TAG, "List size: " + albumArrayList.size());
                for (String s : albumArrayList) {
                    Log.i(TAG, s);
                }
            }

        } catch (Exception e) {
            Log.i(TAG, "Failed to show albuns: " + e);
        }
        return "n";
    }

    /**
     * onPostExecute displays the results of the doInBackgroud and also we
     * can hide progress dialog.
     */
    @Override
    protected void onPostExecute(String msg) {
        pd.dismiss();
        activityReference.get().initButtons();
        createDialogOpts(albumArrayList);
    }

    private void showMessage(String msg) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        );
    }

    private void createDialogOpts(ArrayList<String> albumArrayList) {
        activityReference.get().alertDialogBuilder = new AlertDialog.Builder(activityReference.get());

        LinearLayout layout = new LinearLayout(activityReference.get());
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2, 2, 2, 2);

        TextView tv = new TextView(activityReference.get());
        tv.setText(context.getString(R.string.choose_album));
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        EditText et = new EditText(activityReference.get());
        this.imageTitle = et.getText().toString();
        TextView tv1 = new TextView(activityReference.get());
        tv1.setText(context.getString(R.string.insert_photo_title));


        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(tv1, tv1Params);
        layout.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));


        activityReference.get().alertDialogBuilder.setView(layout);
        activityReference.get().alertDialogBuilder.setTitle("Choose Album");
        // alertDialogBuilder.setMessage("Input Student ID");
        activityReference.get().alertDialogBuilder.setCustomTitle(tv);

        albumArrayAdapter = new ArrayAdapter<>(activityReference.get(), android.R.layout.simple_list_item_1, albumArrayList);
        activityReference.get().alertDialogBuilder.setAdapter(albumArrayAdapter, (DialogInterface dialog, int which) -> {
            this.albumName = albumArrayAdapter.getItem(which);
            this.imageTitle = et.getText().toString();

            if(imageTitle.isEmpty()) {
                showMessage("Image title cannot be empty.");
                return;
            }

            ProgressDialog pd;
            pd = new ProgressDialog(this.activityReference.get());
            pd.setMessage("Adding image to album...");
            pd.setTitle("");
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();

            new AddPhotoTaskPart2(context, this.activityReference.get(), this.albumName, this.imageTitle, pd).execute();



        });
    }

}