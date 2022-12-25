package com.example.layoutfisebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setContentView(R.layout.activity_main);

        TextView tvSingUp = findViewById(R.id.tvSingUp);
        TextView tvSingUp2 = findViewById(R.id.tvSingUp2);
        tvSingUp.setOnClickListener(onClickSignup());
        tvSingUp2.setOnClickListener(onClickSignup());
    }
    private View.OnClickListener onClickSignup(){
    return new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent =  new Intent(LoginActivity.this, SignupActivity.class);
            LoginActivity.this.startActivity(intent);
        }
    };
    }
}