package com.example.mapstreakplaceholder.online;

import com.example.mapstreakplaceholder.online.model.User;

public interface LoginResultHandler {

    void loginSuccessful(User user);

    void loginUnsuccessful(User user, Exception error);

    void registrationSuccessful(User user);

    void registrationUnSuccessful(User user, Exception error);

    void noLogin();

    void logoutSuccessful();
}
