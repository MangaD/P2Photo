package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AddUserToAlbumActivity extends AppCompatActivity {

    private ListView albumListView;
    private ArrayList<String> albumArrayList;
    private ArrayAdapter<String> albumArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_album);

        new AddUserToAlbumTask(AddUserToAlbumActivity.this).execute();
    }

    public ListView getAlbumListView() {
        return albumListView;
    }

    public ArrayAdapter<String> getAlbumArrayAdapter() {
        return albumArrayAdapter;
    }

    public ArrayList<String> getAlbumArrayList() {
        return albumArrayList;
    }

    public void setAlbumArrayAdapter(ArrayAdapter<String> albumArrayAdapter) {
        this.albumArrayAdapter = albumArrayAdapter;
    }

    public void setAlbumArrayList(ArrayList<String> albumArrayList) {
        this.albumArrayList = albumArrayList;
    }

    public void setAlbumListView(ListView albumListView) {
        this.albumListView = albumListView;
    }
}

