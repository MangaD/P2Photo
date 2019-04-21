package pt.ulisboa.tecnico.cmov.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initializeButtons();
    }

    private void initializeButtons() {
        /*
        * button to confirm the creation of a user
        * */
        Button buttonSignIn = findViewById(R.id.button_create_account);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SignUpTask((GlobalClass) getApplicationContext(), SignUpActivity.this).execute();
            }
        });
    }
}