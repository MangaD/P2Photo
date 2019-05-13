package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.AddPhotoTask;

public class AddPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        new AddPhotoTask((GlobalClass) this.getApplicationContext(), AddPhotoActivity.this).execute();
    }
}
