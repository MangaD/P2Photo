package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LoggedInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        initializeButtons();
    }

    private void initializeButtons() {
        /*
        * opens the activity responsible for CREATING ALBUMS
         */
        Button buttonCreateAlbum = findViewById(R.id.button_create_album);
        buttonCreateAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, CreateAlbumActivity.class);
                startActivity(intent);
            }
        });
        /*
        * Find Users
        * */
        Button buttonFindUser = findViewById(R.id.button_find_user);
        buttonFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, FindUserActivity.class);
                startActivity(intent);
            }
        });
        /*
        * add photo to album
        * */
        Button buttonAddPhoto = findViewById(R.id.button_add_photo);
        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, AddPhotoActivity.class);
                startActivity(intent);
            }
        });
        /*
        * add user to album
        * */
        Button buttonAddUserToAlbum = findViewById(R.id.button_add_user_to_album);
        buttonAddUserToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, AddUserToAlbumActivity.class);
                startActivity(intent);
            }
        });
        /*
        * list user albums
        * */
        Button buttonListAlbums = findViewById(R.id.button_list_user_album);
        buttonListAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, ListUserAlbumActivity.class);
                startActivity(intent);
            }
        });
        /*
        * view album
        * */
        Button buttonViewAlbum = findViewById(R.id.button_view_album);
        buttonViewAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, ViewAlbumActivity.class);
                startActivity(intent);
            }
        });

        /*
         * LOG OUT button
         * */
        Button buttonLogOut = findViewById(R.id.button_log_out);
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoggedInActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}
