package pt.ulisboa.tecnico.cmov.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

import android.content.IntentSender;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

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


        // Obtain reference to application context
        GlobalClass globalVariable = (GlobalClass) getApplicationContext();
        // Get mDriveCliet and mDriveResourceCLient from global/application context
        this.mDriveClient = globalVariable.getmDriveClient();
        this.mDriveResourceClient = globalVariable.getmDriveResourceClient();

        ListView mListView = findViewById(R.id.listViewItems);
        mResultsAdapter = new ResultsAdapter(this);
        mListView.setAdapter(mResultsAdapter);

        GlobalClass.PhotoAlbum photoAlbumum = globalVariable.findPhotoAlbum(albumName);
        DriveId driveId = photoAlbumum.getDriveid();

        listFilesInFolder(driveId.asDriveFolder());
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
