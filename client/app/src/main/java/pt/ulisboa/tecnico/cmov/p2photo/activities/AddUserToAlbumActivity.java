package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.AddUserToAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class AddUserToAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_album);

        new AddUserToAlbumTask((GlobalClass) this.getApplicationContext(), AddUserToAlbumActivity.this).execute();
    }
}

