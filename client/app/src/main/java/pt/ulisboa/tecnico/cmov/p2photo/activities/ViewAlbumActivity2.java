package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.ViewAlbumTask;

public class ViewAlbumActivity2 extends AppCompatActivity {

    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album2);

        albumName = getIntent().getStringExtra("ViewAlbumName");

        new ViewAlbumTask((GlobalClass) this.getApplicationContext(), ViewAlbumActivity2.this, albumName).execute();
    }

}
