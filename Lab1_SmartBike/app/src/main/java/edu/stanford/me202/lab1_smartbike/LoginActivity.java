package edu.stanford.me202.lab1_smartbike;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameText;
    private EditText passwordText;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize objects
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);

        // display welcome text
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/magnoliascript.otf");
        welcomeText.setTypeface(font);

        // set password invisible (dots rather than text)
        passwordText.setTransformationMethod(new PasswordTransformationMethod());

        // clear text
        usernameText.setText("");
        passwordText.setText("");
    }

    public void login(View view) {
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        // hardcode valid username and password
        if (username.equals("czhang94") && password.equals("me202rocks")) {
            Intent intent = new Intent(this, ControlActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast warningMessage = Toast.makeText(getApplicationContext(), "Error: incorrect username or password!", Toast.LENGTH_SHORT);
            warningMessage.show();
        }
    }
}
