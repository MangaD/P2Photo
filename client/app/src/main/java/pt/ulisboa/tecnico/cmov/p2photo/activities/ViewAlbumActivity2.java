package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import android.widget.ListView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.widget.DataBufferAdapter;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ResultsAdapter;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.ViewAlbumTask;

public class ViewAlbumActivity2 extends AppCompatActivity {

    private String albumName;

    private static final String TAG = "QueryFilesInFolder";

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private DataBufferAdapter<Metadata> mResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album2);

        albumName = getIntent().getStringExtra("ViewAlbumName");

        new ViewAlbumTask((GlobalClass) this.getApplicationContext(), ViewAlbumActivity2.this, albumName).execute();

/*
        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        ListView mListView = findViewById(R.id.listViewItems);
        mResultsAdapter = new ResultsAdapter(this);
        mListView.setAdapter(mResultsAdapter);

        GlobalClass.PhotoAlbum photoAlbum = globalVariable.findPhotoAlbum(albumName);
        DriveId driveId = photoAlbum.getDriveid();

        listFilesInFolder(driveId.asDriveFolder());
        */
    }


    /**
     * Clears the result buffer to avoid memory leaks as soon
     * as the activity is no longer visible by the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mResultsAdapter.clear();
    }

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private void listFilesInFolder(DriveFolder folder) {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "image/png"))
                .build();
        // [START drive_android_query_children]
        Task<MetadataBuffer> queryTask = mDriveResourceClient.queryChildren(folder, query);
        // END drive_android_query_children]
        queryTask
                .addOnSuccessListener(this,
                        metadataBuffer -> mResultsAdapter.append(metadataBuffer))
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error retrieving files");
                    //showMessage(getString(R.string.query_failed));
                    finish();
                });
    }

}
