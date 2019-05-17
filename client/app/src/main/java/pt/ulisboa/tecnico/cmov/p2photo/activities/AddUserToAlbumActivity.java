package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import pt.ulisboa.tecnico.cmov.p2photo.tasks.AddUserToAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectAddUserToAlbum;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectListUserAlbum;

public class AddUserToAlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_to_album);

        GlobalClass ctx = (GlobalClass) getApplicationContext();

        switch (ctx.getStorageMode()){
            case    "drive":     new AddUserToAlbumTask((GlobalClass) this.getApplicationContext(), AddUserToAlbumActivity.this).execute();
                break;

            case    "wifi":     new WifiDirectAddUserToAlbum(ctx,this);
                break;

        }
    }
}

