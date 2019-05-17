package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.LogOutTask;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initializeButtons();
    }

    private void initializeButtons() {
        /**
         * CREATE ALBUM
         */
        Button buttonCreateAlbum = findViewById(R.id.button_create_album);
        buttonCreateAlbum.setOnClickListener((View view) -> {
            Intent intent = new Intent(MainMenuActivity.this, CreateAlbumActivity.class);
            startActivity(intent);
        });

        /**
         * ADD PHOTO TO ALBUM
         */
        Button buttonAddPhoto = findViewById(R.id.button_add_photo);
        buttonAddPhoto.setOnClickListener((View view) -> {
            GlobalClass ctx = (GlobalClass) getApplicationContext();
            switch (ctx.getStorageMode()){
                case "drive": Intent intent = new Intent(MainMenuActivity.this, AddPhotoActivity.class);
                    startActivity(intent);
                    break;
                case "wifi" : Intent intentWifi = new Intent(MainMenuActivity.this, AddPhotoWifiActivity.class);
                    startActivity(intentWifi);
            }

        });
        /**
         * ADD USER TO ALBUM
         */
        Button buttonAddUserToAlbum = findViewById(R.id.button_add_user_to_album);
        buttonAddUserToAlbum.setOnClickListener((View view) -> {
            Intent intent = new Intent(MainMenuActivity.this, AddUserToAlbumActivity.class);
            startActivity(intent);
        });
        /**
         * LIST USER'S ALBUMS
         */
        Button buttonListAlbums = findViewById(R.id.button_list_user_album);
        buttonListAlbums.setOnClickListener((View view) -> {
            Intent intent = new Intent(MainMenuActivity.this, ListUserAlbumActivity.class);
            startActivity(intent);
        });

        /**
         * LOG OUT
         */
        Button buttonLogOut = findViewById(R.id.button_log_out);
        buttonLogOut.setOnClickListener((View view) ->
            new LogOutTask((GlobalClass) getApplicationContext(), MainMenuActivity.this).execute()
        );

        /*
         * View Photo Test
         *
        Button buttonPhotoTest = findViewById(R.id.button_photo_test);
        buttonPhotoTest.setOnClickListener((View view) -> {
            Intent intent = new Intent(MainMenuActivity.this, ViewPhotoActivity.class);
            startActivity(intent);
        });*/

    }

}
