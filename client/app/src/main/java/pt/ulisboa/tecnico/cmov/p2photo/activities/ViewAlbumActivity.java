package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.ListUserAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.ViewAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectListUserAlbum;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectViewAlbum;

public class ViewAlbumActivity extends AppCompatActivity {

    public ListView albumListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String albumName = getIntent().getStringExtra("ViewAlbumName");

        GlobalClass ctx = (GlobalClass) getApplicationContext();

        switch (ctx.getStorageMode()){
            case    "drive":    new ViewAlbumTask((GlobalClass) this.getApplicationContext(), ViewAlbumActivity.this, albumName).execute();
                break;

            case    "wifi":
                //Toast.makeText(getApplicationContext(),albumName,Toast.LENGTH_SHORT).show();
                new WifiDirectViewAlbum(ctx,this,albumName);
                break;

        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), ListUserAlbumActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

}
