package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText phoneEditText, passwordEditText;
    private Button loginButton;
    private TextView signUpLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpLink = findViewById(R.id.signUpLink);

        // Login Button Logic
        loginButton.setOnClickListener(view -> {
            String phoneNumber = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validatePhoneNumber(phoneNumber) && validatePassword(password)) {
                loginUser(phoneNumber, password);
            }
            else{
                Toast.makeText(LoginActivity.this, "Login Failed! Try Again", Toast.LENGTH_SHORT).show();
            }
        });

        signUpLink.setOnClickListener(view -> {
            Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        });
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10 && TextUtils.isDigitsOnly(phoneNumber);
    }

    private boolean validatePassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    private void loginUser(String phoneNumber, String password) {

        mAuth.signInWithEmailAndPassword(phoneNumber + "@phoneapp.com", password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Login Failed! Try Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
