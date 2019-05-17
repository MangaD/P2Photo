package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.CreateAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectCreateAlbum;

public class CreateAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        Button btn = findViewById(R.id.buttonCreate);
        GlobalClass ctx = (GlobalClass) getApplicationContext();

        switch (ctx.getStorageMode()){
            case    "drive":btn.setOnClickListener((View v) ->
                            new CreateAlbumTask((GlobalClass) getApplicationContext(), CreateAlbumActivity.this).execute()
                    );
                    break;

            case    "wifi": btn.setOnClickListener((View v) ->
                            new WifiDirectCreateAlbum(ctx,this)
                    );
                    break;

        }
    }
}
