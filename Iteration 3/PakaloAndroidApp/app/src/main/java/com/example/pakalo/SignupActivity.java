package com.example.pakalo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirm_password;
    private final String TAG = SignupActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username_al);
        email = findViewById(R.id.email_al);
        password = findViewById(R.id.password_al);
        confirm_password = findViewById(R.id.confirmpassword_al);
    }

    public void authenticate_login(View view) {

        if (username.getText().toString().length() == 0 || email.getText().toString().length() == 0
        || password.getText().toString().length() == 0 || confirm_password.getText().toString().length() == 0) {
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.getText().toString().equals(confirm_password.getText().toString())){
            Toast.makeText(this, "Please Match Passwords", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(),
                password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            //open main activity
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void openLoginActivity(View view) {
        Intent login_intent = new Intent(this, LoginActivity.class);
        startActivity(login_intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent login_intent = new Intent(this, LoginActivity.class);
        startActivity(login_intent);
        finish();
    }
}
