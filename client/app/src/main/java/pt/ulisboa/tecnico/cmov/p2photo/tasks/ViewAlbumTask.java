package pt.ulisboa.tecnico.cmov.p2photo.tasks;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewPhotoActivity;
import pt.ulisboa.tecnico.cmov.p2photo.security.AsymmetricEncryption;
import pt.ulisboa.tecnico.cmov.p2photo.security.SymmetricEncryption;
import pt.ulisboa.tecnico.cmov.p2photo.security.Utility;

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
    private GlobalClass context;

    private String albumName;

    private ArrayList<String> indexURLs;

    private LinkedHashMap<String, String> imagesMap;
    private ArrayList<String> imageArrayList;
    private ArrayAdapter<String> imageArrayAdapter;

    public ViewAlbumTask(GlobalClass ctx, ViewAlbumActivity activity, String albumName) {

        activityReference = new WeakReference<>(activity);
        this.context = ctx;
        this.albumName = albumName;
        this.imageArrayList = new ArrayList<>();
        this.imagesMap = new LinkedHashMap();

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

            /*
             * SECURITY
             */
            SecretKey cipherKey;
            try {
                String encKeyBase64 = conn.getAlbumKey(this.albumName);
                byte[] encKey = Utility.base64ToBytes(encKeyBase64);
                AsymmetricEncryption ae = new AsymmetricEncryption();
                byte[] key = ae.decrypt(context.getPrivKey(), encKey);
                cipherKey = SymmetricEncryption.secretKeyFromByteArray(key);
                Log.d(TAG, cipherKey.toString());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                showMessage("Failed to get album key.");
                return false;
            }

            ArrayList<String> list = conn.getAlbumIndexes(this.albumName);
            if (list == null) {
                conn.disconnect();
                Log.d(TAG, context.getString(R.string.server_contact_fail));
                showMessage(context.getString(R.string.server_contact_fail));
                return false;
            } else {
                this.indexURLs = list;

                Log.d(TAG, "List size: " + list.size());

                for (String encIndexURLBase64 : this.indexURLs) {
                    Log.d(TAG, "Index URL: " + encIndexURLBase64);

                    /*
                     * SECURITY
                     */
                    String indexURL;
                    try {
                        byte[] encImgURL = Utility.base64ToBytes(encIndexURLBase64);
                        SymmetricEncryption se = new SymmetricEncryption();
                        indexURL = se.decryptAES(encImgURL, cipherKey);
                    } catch (Exception e) {
                        showMessage("Error decrypting index URL.");
                        Log.e(TAG, "Error decrypting index URL.\n" + e.getMessage());
                        return false;
                    }
                    Log.d(TAG, "Decypted index url: " + indexURL);

                    URL index = new URL(indexURL);
                    try(BufferedReader in = new BufferedReader(
                            new InputStreamReader(index.openStream()))) {

                        String encImgURLBase64;
                        while ((encImgURLBase64 = in.readLine()) != null && !encImgURLBase64.isEmpty()) {
                            Log.d(TAG, "Encrypted image title and URL: " + encImgURLBase64);

                            /*
                             * SECURITY
                             */
                            String content;
                            try {
                                byte[] encImgURL = Utility.base64ToBytes(encImgURLBase64);
                                SymmetricEncryption se = new SymmetricEncryption();
                                content = se.decryptAES(encImgURL, cipherKey);
                            } catch (Exception e) {
                                showMessage("Error decrypting image URL.");
                                Log.e(TAG, "Error decrypting image URL.\n" + e.getMessage());
                                return false;
                            }
                            Log.d(TAG, "Decypted image title and url: " + content);
                            String[] parts = content.split("\n");
                            if (parts.length < 2) {
                                showMessage("There is something wrong with the image title and url.");
                                return false;
                            }
                            this.imagesMap.put(parts[1], parts[0]);
                            this.imageArrayList.add(parts[0]);
                        }
                    }
                }

                this.activityReference.get().runOnUiThread(() -> {
                    activityReference.get().albumListView = activityReference.get().findViewById(R.id.listViewPhotoItems);
                    this.imageArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                            android.R.layout.simple_list_item_1, this.imageArrayList);
                    activityReference.get().albumListView.setAdapter(this.imageArrayAdapter);

                    activityReference.get().albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
                        Object itemAtPosition = adapter.getItemAtPosition(position);
                        String imageTitle = itemAtPosition.toString();
                        String imageURL = "";

                        int count = 0;
                        for (Map.Entry<String, String> entry : imagesMap.entrySet()) {
                            if(count == position) {
                                imageURL = entry.getKey();
                                break;
                            }
                            count++;
                        }
                        Log.d(TAG, "Image URL at click: " + imageURL);

                        Intent viewPhotoIntent = new Intent(activityReference.get(), ViewPhotoActivity.class);

                        viewPhotoIntent.putExtra("ViewPhotoURL", imageURL);

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
    private void showMessage(String message) {
        activityReference.get().runOnUiThread(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        );
    }
}