package pt.ulisboa.tecnico.cmov.p2photo.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pt.ulisboa.tecnico.cmov.p2photo.GlobalClass;
import pt.ulisboa.tecnico.cmov.p2photo.R;
import pt.ulisboa.tecnico.cmov.p2photo.tasks.SignUpTask;

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
        buttonSignIn.setOnClickListener((View view) -> {
            new SignUpTask((GlobalClass) getApplicationContext(), SignUpActivity.this).execute();
        });
    }
}
