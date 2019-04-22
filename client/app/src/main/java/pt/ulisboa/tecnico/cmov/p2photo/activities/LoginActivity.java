package pt.ulisboa.tecnico.cmov.p2photo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.LoginTask;
import pt.ulisboa.tecnico.cmov.p2photo.R;

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
        buttonLogin.setOnClickListener((View v) -> {
            new LoginTask((GlobalClass) getApplicationContext(), LoginActivity.this).execute();

            // Login is working at the server. Run the server and use it please.
            /*Intent intent = new Intent(LoginActivity.this, DriveLogin.class);
            startActivity(intent);*/
        });
        /*
         * Button that opens the SIGN IN activity
         */
        Button buttonSignIn = findViewById(R.id.button_signIn);
        buttonSignIn.setOnClickListener((View v) -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
