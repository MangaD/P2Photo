package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.GivePermissionTask;

public class GivePermissionActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        String userName = getIntent().getStringExtra("GivePermissionUserName");
        String albumName = getIntent().getStringExtra("GivePermissionAlbumName");
        String publicKeyBase64 = getIntent().getStringExtra("GivePermissionPubKey");
        String encryptedKeyBase64 = getIntent().getStringExtra("GivePermissionEncKey");

        new GivePermissionTask(GivePermissionActivity.this, userName, albumName,
                publicKeyBase64, encryptedKeyBase64).execute();
    }

}
