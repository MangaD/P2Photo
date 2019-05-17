package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ListUserAlbumActivity;
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewAlbumActivity;

public class WifiDirectListUserAlbum {

    private static final String TAG = "WIFI LIST USER ALBUM";

    private WeakReference<ListUserAlbumActivity> activityReference;
    private GlobalClass context;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    public WifiDirectListUserAlbum(GlobalClass ctx, ListUserAlbumActivity activity){
        activityReference = new WeakReference<>(activity);

        this.context = ctx;

        albumArrayList = getUserAlbumsFromFile();

        this.albumListView = activityReference.get().findViewById(R.id.listViewAlbums);
        this.albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                android.R.layout.simple_list_item_1, this.albumArrayList);
        this.albumListView.setAdapter(this.albumArrayAdapter);


        this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
            Object itemAtPosition = adapter.getItemAtPosition(position);
            String itemString = itemAtPosition.toString();

            Intent viewAlbumIntent = new Intent(activityReference.get(), ViewAlbumActivity.class);

            viewAlbumIntent.putExtra("ViewAlbumName",itemString);

            activityReference.get().startActivity(viewAlbumIntent);
        });
    }

    private ArrayList<String> getUserAlbumsFromFile() {
        ArrayList<String> albumList = new ArrayList<>();
        try {
            InputStream inputStream = context.openFileInput(context.getString(R.string.local_storage_file));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    albumList.add(receiveString);
                    //Toast.makeText(context,receiveString,Toast.LENGTH_SHORT).show();
                }
                inputStream.close();

            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return albumList;
    }
}
