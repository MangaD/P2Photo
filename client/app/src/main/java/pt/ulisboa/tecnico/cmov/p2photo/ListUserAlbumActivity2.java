package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Intent;
import android.content.IntentSender;
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

import java.util.ArrayList;

public class ListUserAlbumActivity2 extends AppCompatActivity {

    private static final String TAG = "list_users_album";
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;


    private ListView albumListView;
    private ArrayList<GlobalClass.PhotoAlbum> albumArrayList;
    private ArrayAdapter<GlobalClass.PhotoAlbum> albumArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_album2);

        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        //puts the elements on a list on screen
        albumArrayList = globalVariable.getAlbumList();
        albumListView = findViewById(R.id.listViewAlbums);
        albumArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumArrayList);
        albumListView.setAdapter(albumArrayAdapter);

        albumListView.setOnItemClickListener((adapter, view, position, arg) -> {
            Object itemAtPosition = adapter.getItemAtPosition(position);
            String itemString = itemAtPosition.toString();

            Intent viewAlbumIntent = new Intent(ListUserAlbumActivity2.this, ViewAlbumActivity2.class);

            viewAlbumIntent.putExtra("ViewAlbumName",itemString);

            setDriveVars();

            startActivity(viewAlbumIntent);
        });
    }

    void setDriveVars() {
        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Set mDriveClient and mDriveResourceCliente in global/application context
        globalVariable.setmDriveClient(mDriveClient);
        globalVariable.setmDriveResourceClient(mDriveResourceClient);
    }
}
