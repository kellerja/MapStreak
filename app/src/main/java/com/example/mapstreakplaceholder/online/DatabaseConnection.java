package com.example.mapstreakplaceholder.online;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface DatabaseConnection {

    void saveLocation(LatLng location, List<LatLng> previousPoints);

    void getPointsNear(LatLng location);

    void getPointsForUser();

    void getAllUsersInfo();
}
