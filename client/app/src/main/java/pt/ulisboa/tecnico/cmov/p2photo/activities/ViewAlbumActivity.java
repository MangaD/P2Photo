package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.ViewAlbumTask;

public class ViewAlbumActivity extends AppCompatActivity {

    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        albumName = getIntent().getStringExtra("ViewAlbumName");

        new ViewAlbumTask((GlobalClass) this.getApplicationContext(), ViewAlbumActivity.this, albumName).execute();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), ListUserAlbumActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

}
