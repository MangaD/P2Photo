package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.ListUserAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectListUserAlbum;

public class ListUserAlbumActivity extends AppCompatActivity {

    public ListView albumListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_album);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GlobalClass ctx = (GlobalClass) getApplicationContext();

        switch (ctx.getStorageMode()){
            case    "drive":    new ListUserAlbumTask((GlobalClass) this.getApplicationContext(), ListUserAlbumActivity.this).execute();
                break;

            case    "wifi":     new WifiDirectListUserAlbum(ctx,this);
                break;

        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        Intent myIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
