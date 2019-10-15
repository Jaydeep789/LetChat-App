package com.example.letschat.Authentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letschat.MainActivity;
import com.example.letschat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mAccountTextView;
    private EditText mUserName, mEmail, mPassword;
    private Button mRegister;

    private String UserID;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");

        mAccountTextView = findViewById(R.id.register_create_account);
        mUserName = findViewById(R.id.register_username);
        mEmail = findViewById(R.id.register_email);
        mPassword = findViewById(R.id.register_password);
        mRegister = findViewById(R.id.register_button_text);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

    }

    private void registerUser() {
        final String mUserText = mUserName.getText().toString();
        String mUserEmailText = mEmail.getText().toString();
        String mPasswordText = mPassword.getText().toString();

        if ((TextUtils.isEmpty(mUserText)) || (TextUtils.isEmpty(mUserEmailText)) || (TextUtils.isEmpty(mPasswordText))) {
            Toast.makeText(this, "All Fields are required", Toast.LENGTH_SHORT).show();
        } else if (mPasswordText.length() < 6) {
            Toast.makeText(this, "Password must have atleast 6 characters", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(mUserEmailText, mPasswordText)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mCurrentUser = mAuth.getCurrentUser();
                                assert mCurrentUser != null;
                                UserID = mCurrentUser.getUid();

                                mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(UserID);

                                HashMap<String, String> registerMap = new HashMap<>();
                                registerMap.put("id", UserID);
                                registerMap.put("username", mUserText);
                                registerMap.put("imageURL", "default");
                                registerMap.put("status","offline");
                                registerMap.put("search",mUserText.toLowerCase());

                                mDatabaseReference.setValue(registerMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            sendUserToMainPage();
                                            Toast.makeText(RegisterActivity.this, "Welcome " + mUserText, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration Failed. Please try again", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    private void sendUserToMainPage() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
