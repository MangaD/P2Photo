package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.FindUserTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class FindUserActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        String albumName = getIntent().getStringExtra("ViewAlbumName");

        new FindUserTask(FindUserActivity.this, albumName).execute();
    }

}
