package edu.stanford.me202.smartbike;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity {

    private final static String TAG = SignUpActivity.class.getSimpleName();

    // FireBase Auth
    private FirebaseAuth fireDBAuth;

    private ProgressDialog progressDialog;

    private String username;
    private String password;

    @BindView(R.id.username_text) EditText username_raw;
    @BindView(R.id.password_text) EditText password_raw;
    @BindView(R.id.signup_btn) ImageButton signupButton;
    @BindView(R.id.signUpText) TextView signuptext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        // display signup text
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/magnoliascript.otf");
        signuptext.setTypeface(font);

        // FireDB instance
        fireDBAuth = FirebaseAuth.getInstance();

        // instantiate progress dialog to show authentication
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void signUp(View v) {
        username = username_raw.getText().toString().trim();
        password = password_raw.getText().toString().trim();

        if (validate(username, password)) {
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();

            fireDBAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        username_raw.setText("");
                        password_raw.setText("");
                        progressDialog.hide();
                        Toast.makeText(SignUpActivity.this, R.string.signup_success, Toast.LENGTH_SHORT).show();

                       // progressDialog.dismiss();
                        // redirect to login in
                        Intent intent = new Intent(SignUpActivity.this, ControlActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        progressDialog.hide();
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            username_raw.setError(getString(R.string.signup_changeemail));
                            Toast.makeText(SignUpActivity.this, R.string.signup_emailcollision, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, R.string.signup_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        } else {
            // sign up fails
            Toast.makeText(SignUpActivity.this, R.string.signup_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public void signInRedirect(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // helper function to validate user email address and password
    private boolean validate(String email, String password) {
        boolean valid = true;

        // check email address format
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            username_raw.setError(getString(R.string.signup_emailerror));
            valid = false;
        } else {
            username_raw.setError(null);
        }

        // check password format
        if (password.isEmpty() || password.length() <= 5) {
            password_raw.setError(getString(R.string.signup_passworderror));
            valid = false;
        } else {
            password_raw.setError(null);
        }

        return valid;
    }
}
