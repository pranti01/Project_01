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
import com.xyz.moneytrail.R;

import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button btnReg;
    private TextView mLogin;

    private ProgressDialog mDialog;


    //Firebase...

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(this);
        registration();
    }


    private void registration ()
    {
        mEmail = findViewById(R.id.email_reg);
        mPass = findViewById(R.id.password_reg);
        btnReg = findViewById(R.id.btn_reg);
        mLogin = findViewById(R.id.login_here);

        btnReg.setOnClickListener(new View.OnClickListener() {
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

                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            mDialog.dismiss();
                            Toast.makeText(RegistrationActivity.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            }
        });

    }
}