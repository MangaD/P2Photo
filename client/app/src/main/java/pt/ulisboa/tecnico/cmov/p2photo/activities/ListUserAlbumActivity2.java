package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.ListUserAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class ListUserAlbumActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_album2);

        new ListUserAlbumTask((GlobalClass) this.getApplicationContext(), ListUserAlbumActivity2.this).execute();
    }
}
