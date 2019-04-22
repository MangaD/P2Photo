package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.CreateAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class CreateAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        Button btn = findViewById(R.id.buttonCreate);

        btn.setOnClickListener((View v) ->
                new CreateAlbumTask((GlobalClass) getApplicationContext(), CreateAlbumActivity.this).execute()
        );
    }

}
