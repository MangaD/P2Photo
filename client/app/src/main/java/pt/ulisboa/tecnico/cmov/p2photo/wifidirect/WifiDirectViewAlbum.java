package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

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
import pt.ulisboa.tecnico.cmov.p2photo.activities.ViewPhotoActivity;

public class WifiDirectViewAlbum {

    private static final String TAG = "WIFI LIST USER ALBUM";

    private WeakReference<ViewAlbumActivity> activityReference;
    private GlobalClass context;

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    private String albumName;

    public WifiDirectViewAlbum(GlobalClass ctx, ViewAlbumActivity activity,String albumName){
        activityReference = new WeakReference<>(activity);

        this.context = ctx;

        albumArrayList = getAlbumFromFile();
        this.albumName=albumName;

        this.albumListView = activityReference.get().findViewById(R.id.listViewPhotoItems);
        this.albumArrayAdapter = new ArrayAdapter<>(activityReference.get(),
                android.R.layout.simple_list_item_1, this.albumArrayList);
        this.albumListView.setAdapter(this.albumArrayAdapter);


        this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
            Object itemAtPosition = adapter.getItemAtPosition(position);
            String itemString = itemAtPosition.toString();

            Intent viewPhotoIntent = new Intent(activityReference.get(), ViewPhotoActivity.class);

            viewPhotoIntent.putExtra("ViewPhotoName",itemString);

            activityReference.get().startActivity(viewPhotoIntent);
        });
    }
    //TODO change this to get photo names
    private ArrayList<String> getAlbumFromFile() {
        ArrayList<String> albumList = new ArrayList<>();
        try {
            InputStream inputStream = context.openFileInput(albumName+".index");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    albumList.add(receiveString);
                    Toast.makeText(context,receiveString,Toast.LENGTH_SHORT).show();
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
