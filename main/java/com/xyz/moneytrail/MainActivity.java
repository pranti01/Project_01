package com.xyz.moneytrail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.xyz.moneytrail.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button btnLogin;
    private TextView mForgotPassword;
    private TextView mSignupHere;


    private ProgressDialog mDialog;

    //Firebase...

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mDialog = new ProgressDialog(this);

        loginDetails();
    }


    private void loginDetails()
    {
        mEmail = findViewById(R.id.email_login);
        mPass = findViewById(R.id.password_login);
        btnLogin = findViewById(R.id.btn_login);

        mSignupHere = findViewById(R.id.signup_reg);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString().trim();
                String pass = mPass.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    mEmail.setError("Email is required!");
                    mEmail.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(pass))
                {
                    mPass.setError("Password is required!");
                    mPass.requestFocus();
                    return;
                }

                mDialog.setMessage("Processing...");
                mDialog.show();

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            mDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            Toast.makeText(MainActivity.this, "User has been logged in successfully!", Toast.LENGTH_LONG).show();
                        } else
                        {

                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Login Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        mSignupHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });


    }




}
