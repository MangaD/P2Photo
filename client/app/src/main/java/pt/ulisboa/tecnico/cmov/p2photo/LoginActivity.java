package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        initializeButtons();
    }

    private void initializeButtons() {
        /*
        * LOGIN button
         */
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //boolean valid = login();
                //if (valid) {
                    Intent intent = new Intent(LoginActivity.this, DriveLogin.class);
                    startActivity(intent);
                //} else {
                //    String msg = "Login invalid.";
                //    Log.d("LoginActivity", msg);
                //    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                //}
            }
        });
        /*
        * Button that opens the SIGN IN activity
         */
        Button buttonSignIn = findViewById(R.id.button_signIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean login() {
        GlobalClass context = (GlobalClass) getApplicationContext();
        ServerConnection conn = context.getConnection();
        try {
            conn.connect();
        } catch (IOException e) {
            String msg = "Failed to connect to the server.";
            Log.d("LoginActivity", msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            return false;
        }

        EditText usernameEdit = findViewById(R.id.login_username);
        String username = usernameEdit.getText().toString();
        EditText passwordEdit = findViewById(R.id.login_password);
        String password = passwordEdit.getText().toString();
        try {
            conn.login(username, password);
        } catch (IOException e) {
            String msg = "Failed to contact the server.";
            Log.d("LoginActivity", msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

}
