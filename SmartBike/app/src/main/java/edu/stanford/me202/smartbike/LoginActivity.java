package edu.stanford.me202.smartbike;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private final static String TAG = LoginActivity.class.getSimpleName();

    // Firebase Auth
    private FirebaseAuth fireDBAuth;
    private ProgressDialog progressDialog;

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

        fireDBAuth = FirebaseAuth.getInstance();

        if (fireDBAuth.getCurrentUser() != null) {
            Log.d(TAG, fireDBAuth.getCurrentUser().getUid());
            // redirect to control activity
            Intent intent = new Intent(LoginActivity.this, ControlActivity.class);
            startActivity(intent);
            finish();
        }

        progressDialog = new ProgressDialog(this);

        // hardcode for quick login
        usernameText.setText("czhang94@stanford.edu");
        passwordText.setText("smartbike");

    }

    public void login(View view) {
        final String username = usernameText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        fireDBAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.hide();
                    Intent intent = new Intent(LoginActivity.this, ControlActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    progressDialog.hide();
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        usernameText.setText("");
                        passwordText.setText("");
                        Toast.makeText(LoginActivity.this, R.string.login_notMember, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        passwordText.setText("");
                        Toast.makeText(LoginActivity.this, R.string.login_wrongPassword, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void signUpRedirect(View v) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
