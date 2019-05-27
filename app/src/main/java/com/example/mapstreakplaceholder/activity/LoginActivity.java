package com.example.mapstreakplaceholder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mapstreakplaceholder.R;
import com.example.mapstreakplaceholder.online.FirebaseLoginConnection;
import com.example.mapstreakplaceholder.online.LoginConnection;
import com.example.mapstreakplaceholder.online.LoginResultHandler;
import com.example.mapstreakplaceholder.online.model.User;

public class LoginActivity extends AppCompatActivity implements LoginResultHandler {

    private EditText emailEditText;
    private EditText passwordEditText;
    private LoginConnection loginConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        emailEditText = (EditText) findViewById(R.id.login_email_editText);
        passwordEditText = (EditText) findViewById(R.id.login_password_editText);

        loginConnection = new FirebaseLoginConnection(this);
    }

    public void loginButtonClick(View view) {
        String email = emailEditText.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(this, "Email must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = passwordEditText.getText().toString();
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User(email, password);
        loginConnection.signInWithEmail(user);
    }

    public void laterButtonClick(View view) {
        loginConnection.signInAnonymously();
    }

    public void registerButtonClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void loginSuccessful(User user) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void loginUnsuccessful(User user, Exception error) {
        Toast.makeText(this, "Login failed with message: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registrationSuccessful(User user) {
    }

    @Override
    public void registrationUnSuccessful(User user, Exception error) {
    }

    @Override
    public void noLogin() {
    }

    @Override
    public void logoutSuccessful() {
    }
}
