package edu.stanford.me202.lab1_smartbike;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/magnoliascript.otf");
        welcomeText.setTypeface(font);
    }
}
