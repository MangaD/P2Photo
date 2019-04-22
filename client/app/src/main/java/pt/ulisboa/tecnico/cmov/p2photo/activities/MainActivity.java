package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.p2photo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeButtons();
    }

    private void initializeButtons() {
        /*
        * Button that starts the app in CLOUD mode
        * */
        Button buttonCloudMode = findViewById(R.id.button_cloud);
        buttonCloudMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        /*
        * Button that starts the app in WIRELESS mode
        * */
        Button buttonWirelessMode = findViewById(R.id.button_wireless);
        buttonWirelessMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

}
