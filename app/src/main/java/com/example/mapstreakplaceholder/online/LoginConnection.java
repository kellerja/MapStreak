package com.example.mapstreakplaceholder.online;

import com.example.mapstreakplaceholder.online.model.User;

public interface LoginConnection {

    void signInWithEmail(User user);

    void signOut();

    void registerWithEmail(User user);

    void signInAnonymously();
}
