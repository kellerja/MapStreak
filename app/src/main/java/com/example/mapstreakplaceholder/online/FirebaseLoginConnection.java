package com.example.mapstreakplaceholder.online;

import android.support.annotation.NonNull;

import com.example.mapstreakplaceholder.online.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class FirebaseLoginConnection implements LoginConnection {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private LoginResultHandler resultHandler;
    private DatabaseReference userReference;
    private Random random;

    public FirebaseLoginConnection(final LoginResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference("user");

        random = new Random();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    resultHandler.loginSuccessful(new User(user));
                } else {
                    resultHandler.noLogin();
                }
            }
        };
    }

    @Override
    public void signInWithEmail(final User user) {
        mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    resultHandler.loginUnsuccessful(user, task.getException());
                    return;
                }
                resultHandler.loginSuccessful(new User(task.getResult().getUser()));
            }
        });
    }

    @Override
    public void signOut() {
        mAuth.signOut();
        resultHandler.logoutSuccessful();
    }

    @Override
    public void registerWithEmail(final User user) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            resultHandler.registrationUnSuccessful(user, task.getException());
                            return;
                        }
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(user.getName() == null ? "User" + random.nextInt() : user.getName()).build();
                        task.getResult().getUser().updateProfile(profileUpdates);
                        resultHandler.registrationSuccessful(new User(task.getResult().getUser()));
                    }
                });
    }

    @Override
    public void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().getUser().getDisplayName() == null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName("Guest" + random.nextInt()).build();
                                task.getResult().getUser().updateProfile(profileUpdates);
                            }
                            resultHandler.loginSuccessful(new User(task.getResult().getUser()));
                        } else {
                            resultHandler.loginUnsuccessful(null, task.getException());
                        }
                    }
                });
    }
}
