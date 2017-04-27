package edu.stanford.me202.smartbike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity {
    // Pesudo UserDB
    static Map<String, String> userDB = new HashMap<>();
    private String username;
    private String password;

    @BindView(R.id.username_text) EditText username_raw;
    @BindView(R.id.password_text) EditText password_raw;
    @BindView(R.id.signup_btn) Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void signUp(View v) {
        username = username_raw.getText().toString();
        password = password_raw.getText().toString();

        username_raw.setText("");
        password_raw.setText("");

        if (userDB.containsKey(username)) {
            Toast.makeText(SignUpActivity.this, "ERROR: usernmae already used!", Toast.LENGTH_SHORT).show();
        } else {
            userDB.put(username, password);
            Toast.makeText(SignUpActivity.this, "Welcome! You are now a member!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
