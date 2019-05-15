package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.ServerConnection;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.LoginTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private GlobalClass context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        context = (GlobalClass) getApplicationContext();

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        initializeButtons();
    }

    private void initializeButtons() {
        /*
         * LOGIN button
         */
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener((View v) -> {
            if(!setSettings()) {
                return;
            }
            new LoginTask((GlobalClass) getApplicationContext(), LoginActivity.this).execute();
        });
        /*
         * Button that opens the SIGN UP activity
         */
        Button buttonSignIn = findViewById(R.id.button_signIn);
        buttonSignIn.setOnClickListener((View v) -> {
            if(!setSettings()) {
                return;
            }
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private boolean setSettings() {
        if(!setServerAddress()) {
            return false;
        } else if(!setStorageMode()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean setServerAddress() {
        ServerConnection conn = context.getConnection();
        String address = sp.getString("address", getString(R.string.default_ip));
        conn.addr = address;

        if (conn.addr.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_address), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.invalid_address));
            return false;
        }

        String port_s = sp.getString("port", getString(R.string.default_port));
        try {
            conn.port = Integer.parseInt(port_s);
            if (conn.port <= 1023 || conn.port > 65535) {
                Toast.makeText(getApplicationContext(), getString(R.string.invalid_port), Toast.LENGTH_LONG).show();
                Log.d("LoginActivity", getString(R.string.invalid_port));
                return false;
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_port), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.invalid_port));
            return false;
        }
        Log.d("LoginActivity", "Address: " + conn.addr + "\n Port: " + conn.port);
        return true;
    }

    private boolean setStorageMode() {
        String mode = sp.getString("storage_preference", getString(R.string.storage_default));
        Log.d("LoginActivity", mode);
        if(mode.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.invalid_mode), Toast.LENGTH_LONG).show();
            Log.d("LoginActivity", getString(R.string.invalid_mode));
            return false;
        }
        context.storageMode = mode;
        return true;
    }

    /**
     * https://stackoverflow.com/questions/47488899/how-to-show-the-settingsactivity-when-pressing-on-settings-button
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.settings){
            startActivity(new Intent(LoginActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
