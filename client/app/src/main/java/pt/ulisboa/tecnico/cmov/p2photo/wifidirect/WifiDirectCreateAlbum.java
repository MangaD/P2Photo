package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.Scanner;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.CreateAlbumActivity;

public class WifiDirectCreateAlbum {

    private static final String TAG = "WIFI CREATE ALBUM";
    private WeakReference<CreateAlbumActivity> activityReference;
    private GlobalClass context;
    private String albumName;

    public WifiDirectCreateAlbum(GlobalClass ctx, CreateAlbumActivity activity){
        activityReference = new WeakReference<>(activity);

        this.context = ctx;


        EditText albumNameEdit = activityReference.get().findViewById(R.id.albumName);
        albumName = albumNameEdit.getText().toString();

        if (albumName.isEmpty()) {
            String msg = context.getString(R.string.album_empty_name);
            Log.d(TAG, msg);
        }
        else {
            createAlbumFile(albumName,context);
            //readFromFile(context,FILENAME);
            //readFromFile(context,"something.index");
        }
    }

    private void createAlbumFile(String data,Context context){
        if(!albumAlreadyExists(data)){
            writeToFile(data,context,context.getString(R.string.local_storage_file));
            createPhotoIndex(data,context);
        }
    }

    private boolean albumAlreadyExists(String albumName) {
        try {
            InputStream inputStream = context.openFileInput(context.getString(R.string.local_storage_file));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    if(receiveString.equals(albumName)){
                        return true;
                    }
                    //Toast.makeText(context,receiveString,Toast.LENGTH_SHORT).show();
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("create album activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("create album activity", "Can not read file: " + e.toString());
        }
        return false;
    }

    private void createPhotoIndex(String data, Context context) {
        writeToFile("",context,data+".index"); // create a empty index file for each album
    }


    private void writeToFile(String data,Context context, String filename) {
        try {
            if (readFromFile(context,filename).equals("")){
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            }
            else {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_APPEND));
                outputStreamWriter.append("\n"+data);
                outputStreamWriter.close();
            }
            Toast.makeText(context,"album created",Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(context,"cannot create album",Toast.LENGTH_SHORT).show();
        }
    }

    private String readFromFile(Context context,String filename) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    //Toast.makeText(context,receiveString,Toast.LENGTH_SHORT).show();
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


}
