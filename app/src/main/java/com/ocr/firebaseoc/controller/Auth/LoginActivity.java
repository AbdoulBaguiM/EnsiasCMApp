package com.ocr.firebaseoc.controller.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ocr.firebaseoc.R;
import com.ocr.firebaseoc.controller.HomeActivity;
import com.ocr.firebaseoc.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private TextInputEditText username, password;
    private Button loginButton, signUpButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mainLayout = findViewById(R.id.mainLayout);
        username = findViewById(R.id.editTextUserName);
        password = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constants.checkIsEmpty(username.getText().toString()) || Constants.checkIsEmpty(password.getText().toString()))
                    showSnackBar("Veuillez renseigner vos informations");
                else
                    startLoginWithParameters(username.getText().toString(),password.getText().toString());
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignUpActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startHomeActivity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    private void startSignUpActivity() {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
    }

    private void startLoginWithParameters(String username, String password) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI
                            startHomeActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            showSnackBar("Identifiant ou mot de passe incorrect");
                        }
                    }
                });
    }

    // Show Snack Bar with a message
    private void showSnackBar( String message){
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}