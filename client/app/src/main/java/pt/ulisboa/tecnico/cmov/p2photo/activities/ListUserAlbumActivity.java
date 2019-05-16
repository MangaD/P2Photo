package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.ListUserAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class ListUserAlbumActivity extends AppCompatActivity {

    public ListView albumListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_album);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new ListUserAlbumTask((GlobalClass) this.getApplicationContext(), ListUserAlbumActivity.this).execute();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
