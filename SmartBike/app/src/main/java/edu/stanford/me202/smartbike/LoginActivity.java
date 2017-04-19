package edu.stanford.me202.smartbike;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.welcomeText)
    TextView welcomeText;
    @BindView(R.id.username)
    EditText usernameText;
    @BindView(R.id.password)
    EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // display welcome text
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/magnoliascript.otf");
        welcomeText.setTypeface(font);

        // set password invisible (dots rather than text)
        passwordText.setTransformationMethod(new PasswordTransformationMethod());

        // hardcode for quick login
        usernameText.setText("czhang94");
        passwordText.setText("smartbike");
    }

    public void login(View view) {
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        // hardcode valid username and password
        if (username.equals("czhang94") && password.equals("smartbike")) {
            Intent intent = new Intent(this, ControlActivity.class);
            startActivity(intent);
            // user will not return to login activity one they leave
            finish();
        } else {
            Toast warningMessage = Toast.makeText(getApplicationContext(), "Error: incorrect username or password!", Toast.LENGTH_SHORT);
            warningMessage.show();
            // clear text
            usernameText.setText("");
            passwordText.setText("");
        }
    }
}
