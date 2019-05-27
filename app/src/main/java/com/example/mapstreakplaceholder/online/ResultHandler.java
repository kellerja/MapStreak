package com.example.mapstreakplaceholder.online;

import com.example.mapstreakplaceholder.online.model.DataEntry;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface ResultHandler {
    void handlePointsByUser(List<LatLng> points);

    void handlePointsNearLocation(List<DataEntry> entry);

    void removePathTo(LatLng location);

    void handleUsersInfo(List<DataEntry> users);
}
