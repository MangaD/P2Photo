package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.FindUserTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

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

        new FindUserTask(FindUserActivity.this).execute();
    }

    public ArrayAdapter<String> getUserArrayAdapter() {
        return userArrayAdapter;
    }

    public ArrayList<String> getUserArrayList() {
        return userArrayList;
    }

    public ListView getUserListView() {
        return userListView;
    }

    public void setUserArrayAdapter(ArrayAdapter<String> userArrayAdapter) {
        this.userArrayAdapter = userArrayAdapter;
    }

    public void setUserArrayList(ArrayList<String> userArrayList) {
        this.userArrayList = userArrayList;
    }

    public void setUserListView(ListView userListView) {
        this.userListView = userListView;
    }
}
