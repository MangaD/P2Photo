package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

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

        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();


        //puts the elements on a list on screen
        userArrayList = new ArrayList<>();
        userListView = findViewById(R.id.listUsers);
        userArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userArrayList);
        userListView.setAdapter(userArrayAdapter);

        userListView.setOnItemClickListener((adapter, view, position, arg) -> {
            Object itemAtPosition = adapter.getItemAtPosition(position);
            String itemString = itemAtPosition.toString();

            Intent viewAlbumIntent = new Intent(FindUserActivity.this, AddUserToAlbumActivity.class);

            startActivity(viewAlbumIntent);
        });
    }
}
