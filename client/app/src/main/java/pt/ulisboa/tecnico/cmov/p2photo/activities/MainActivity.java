package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;

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
        buttonCloudMode.setOnClickListener((View view) -> {
            setServerAddress();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        /*
         * Button that starts the app in WIRELESS mode
         * */
        Button buttonWirelessMode = findViewById(R.id.button_wireless);
        buttonWirelessMode.setOnClickListener((View view) -> {
            setServerAddress();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }

    private void setServerAddress() {
        ServerConnection conn = ((GlobalClass) getApplicationContext()).getConnection();
        EditText address = findViewById(R.id.serverIPAddr);
        EditText port = findViewById(R.id.serverPort);
        // TODO - verify if input is valid
        conn.addr = address.getText().toString();
        conn.port = Integer.parseInt(port.getText().toString());
        Log.d("MainActivity", "Address: " + conn.addr + "\n Port: " + conn.port);
    }

}
