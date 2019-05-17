package pt.ulisboa.tecnico.cmov.p2photo.wifidirect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.activities.MainMenuActivity;

public class WifiDirectAddPhoto extends AppCompatActivity {

    private GlobalClass ctx;

    private Bitmap imageBitmap;


    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct_add_photo);

        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            imageBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ctx = (GlobalClass) getApplicationContext();

        initializeButtons();
    }

    private void initializeButtons() {
        albumArrayList = getUserAlbumsFromFile();

        this.albumListView = this.findViewById(R.id.listAlbumsAddPhotoWifi);
        this.albumArrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, this.albumArrayList);
        this.albumListView.setAdapter(this.albumArrayAdapter);


        this.albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
            Object itemAtPosition = adapter.getItemAtPosition(position);
            String albumName = itemAtPosition.toString();

            EditText photoNameEdit = this.findViewById(R.id.albumName);
            String photoName = photoNameEdit.getText().toString();

            if (photoName.isEmpty()) {
                Toast.makeText(this,"Photo Name can't be empty",Toast.LENGTH_SHORT).show();
            }
            else {
                //save photo to internal storage
                photoName +=".png";
                try (FileOutputStream out = new FileOutputStream(photoName)) {
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (IOException e) {
                    Toast.makeText(this,"ERROR",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                //add to index of album
                writeToFile(photoName,this,albumName+".index");
                Intent mainMenuIntent = new Intent(this, MainMenuActivity.class);

                this.startActivity(mainMenuIntent);
            }
        });
    }

    private ArrayList<String> getUserAlbumsFromFile() {
        ArrayList<String> albumList = new ArrayList<>();
        try {
            InputStream inputStream = ctx.openFileInput(ctx.getString(R.string.local_storage_file));

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

    private void writeToFile(String data, Context context, String filename) {
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
            Toast.makeText(context,"photo created",Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            Toast.makeText(context,"cannot create photo",Toast.LENGTH_SHORT).show();
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
