package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.CreateAlbumTask;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.FindUserTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectCreateAlbum;
import pt.ulisboa.tecnico.cmov.p2photo.wifidirect.WifiDirectFindUser;

public class FindUserActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        String albumName = getIntent().getStringExtra("FindActivityAlbumName");
        String encryptedKeyBase64 = getIntent().getStringExtra("FindActivityEncKey");

        GlobalClass ctx = (GlobalClass) getApplicationContext();

        switch (ctx.getStorageMode()){
            case    "drive":new FindUserTask(FindUserActivity.this, albumName, encryptedKeyBase64).execute();
                break;

            case    "wifi": new WifiDirectFindUser(ctx,this,albumName);
                break;
        }

    }

}
