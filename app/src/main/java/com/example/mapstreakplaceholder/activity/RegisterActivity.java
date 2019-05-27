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


public class RegisterActivity extends AppCompatActivity implements LoginResultHandler {

    private LoginConnection loginConnection;
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText passwordAgainEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        loginConnection = new FirebaseLoginConnection(this);

        emailEditText = (EditText) findViewById(R.id.register_email_editText);
        nameEditText = (EditText) findViewById(R.id.register_name_editText);
        passwordEditText = (EditText) findViewById(R.id.register_password_editText);
        passwordAgainEditText = (EditText) findViewById(R.id.register_password_again_editText);
    }

    public void registerButtonClick(View view) {
        String email = emailEditText.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(this, "Email must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = nameEditText.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "Name must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = passwordEditText.getText().toString();
        if (password.isEmpty()) {
            Toast.makeText(this, "Password must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String passwordAgain = passwordAgainEditText.getText().toString();
        if (!passwordAgain.equals(password)) {
            Toast.makeText(this, "Passwords must match", Toast.LENGTH_SHORT).show();
            return;
        }
        loginConnection.registerWithEmail(new User(email, password, name));
    }

    public void cancelButtonClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void loginSuccessful(User user) {
    }

    @Override
    public void loginUnsuccessful(User user, Exception error) {
    }

    @Override
    public void registrationSuccessful(User user) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void registrationUnSuccessful(User user, Exception error) {
        Toast.makeText(this, "Registration failed with message: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noLogin() {
    }

    @Override
    public void logoutSuccessful() {
    }
}
