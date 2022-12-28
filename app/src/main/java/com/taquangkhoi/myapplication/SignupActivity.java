package com.taquangkhoi.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    TextView tvLogin;
    TextView edtEmail;
    TextView edtPassword;
    TextView edtConfirmPassword;
    Button btnSignup;
    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        tvLogin = findViewById(R.id.tvLogin);


        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);

        mAuth = FirebaseAuth.getInstance();

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupActivity.this.finish();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                String confirmPassword = edtConfirmPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    return;
                } else {
                    if (password.equals(confirmPassword)) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(SignupActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "onComplete: " + task.getResult().getUser().getUid());
                                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(SignupActivity.this, task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        Log.i(TAG, "onComplete error: " + task.getException().getMessage());
                                    }
                                });
                    }
                    else {
                        Toast.makeText(SignupActivity.this, "Password and confirm password not match", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onClick: password not match");
                        return;
                    }
                }
            }
        });
    }
}