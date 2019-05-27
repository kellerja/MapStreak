package com.example.mapstreakplaceholder.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.example.mapstreakplaceholder.activity.MapsActivity;
import com.example.mapstreakplaceholder.location.callback.LocationSettingsResultCallback;
import com.example.mapstreakplaceholder.online.DatabaseConnection;
import com.example.mapstreakplaceholder.online.FirebaseConnection;
import com.example.mapstreakplaceholder.online.ResultHandler;
import com.example.mapstreakplaceholder.online.model.DataEntry;
import com.example.mapstreakplaceholder.permissions.Permissions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationServiceManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultHandler {

    private GoogleApiClient mGoogleApiClient;
    private Activity caller;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private List<LatLng> points;
    private DatabaseConnection database;
    private PendingResult<Status> requestLocationUpdates;

    public LocationServiceManager(Activity caller) {
        this.caller = caller;
        database = new FirebaseConnection(this);
        database.getPointsForUser();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(caller)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocationRequest();
    }

    public void startGoogleApiClient() {
        mGoogleApiClient.connect();
    }

    public void stopGoogleApiClient() {
        mGoogleApiClient.disconnect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        new LocationSettingsResultCallback(mLocationRequest, mGoogleApiClient, caller);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                caller.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, Permissions.REQUEST_LOCATION_PERMISSIONS.getPermissionCode());
            }
            return;
        }

        onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        startLocationUpdates();
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                caller.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, Permissions.REQUEST_LOCATION_PERMISSIONS.getPermissionCode());
            }
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            requestLocationUpdates = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public synchronized void onLocationChanged(Location location) {
        System.out.println("!!!!!!!!!!! " + location);
        if (location != null && points != null) {
            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            database.getPointsNear(currentPosition);
        }
        if (location == null ||
                (mLastLocation != null &&
                        String.format(Locale.ENGLISH, "%.4f", location.getLatitude()).equals(String.format(Locale.ENGLISH, "%.4f", mLastLocation.getLatitude())) &&
                        String.format(Locale.ENGLISH, "%.4f", location.getLongitude()).equals(String.format(Locale.ENGLISH, "%.4f", mLastLocation.getLongitude())))) {
            return;
        }
        if (mLastLocation != null) {
            BigDecimal currentLocationLatitude = BigDecimal.valueOf(location.getLatitude()).setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal currentLocationLongitude = BigDecimal.valueOf(location.getLongitude()).setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal prevLocationLatitude = BigDecimal.valueOf(mLastLocation.getLatitude()).setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal prevLocationLongitude = BigDecimal.valueOf(mLastLocation.getLongitude()).setScale(4, BigDecimal.ROUND_HALF_UP);
            if (currentLocationLatitude.subtract(prevLocationLatitude).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0 || currentLocationLongitude.subtract(prevLocationLongitude).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0) {
                List<LatLng> pointsBetween = fillPointsBetween(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()), new ArrayList<LatLng>());
                if (SphericalUtil.computeDistanceBetween(pointsBetween.get(0), new LatLng(location.getLatitude(), location.getLongitude())) < SphericalUtil.computeDistanceBetween(pointsBetween.get(0), new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))) {
                    Collections.reverse(pointsBetween);
                }
                if (database instanceof FirebaseConnection) {
                    ((FirebaseConnection) database).saveLocationLowPriority(new ArrayList<>(pointsBetween), new ArrayList<>(points), new ArrayList<LatLng>(), new LatLng(location.getLatitude(), location.getLongitude()));
                }
                points.addAll(pointsBetween);
            }
        }
        mLastLocation = location;
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (points != null) {
            database.saveLocation(currentPosition, points);
            points.add(currentPosition);
            //database.getPointsNear(currentPosition);
        }
        if (caller instanceof MapsActivity) {
            ((MapsActivity) caller).updateMap(mLastLocation);
        }
    }

    private List<LatLng> fillPointsBetween(LatLng from, LatLng to, List<LatLng> points) {
        BigDecimal currentLocationLatitude = BigDecimal.valueOf(from.latitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal currentLocationLongitude = BigDecimal.valueOf(from.longitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal prevLocationLatitude = BigDecimal.valueOf(to.latitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal prevLocationLongitude = BigDecimal.valueOf(to.longitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        if (currentLocationLatitude.subtract(prevLocationLatitude).abs().compareTo(BigDecimal.valueOf(0.0001)) <= 0 && currentLocationLongitude.subtract(prevLocationLongitude).abs().compareTo(BigDecimal.valueOf(0.0001)) <= 0) {
            return points;
        }
        LatLng middle = SphericalUtil.interpolate(from, to, 0.5);
        BigDecimal middleLocationLatitude = BigDecimal.valueOf(middle.latitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal middleLocationLongitude = BigDecimal.valueOf(middle.longitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        if (middleLocationLatitude.subtract(prevLocationLatitude).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0 || middleLocationLongitude.subtract(prevLocationLongitude).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0) {
            fillPointsBetween(middle, to, points);
        }
        points.add(middle);
        if (currentLocationLatitude.subtract(middleLocationLatitude).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0 || currentLocationLongitude.subtract(middleLocationLongitude).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0) {
            fillPointsBetween(from, middle, points);
        }
        return points;
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    @Override
    public void handlePointsByUser(List<LatLng> points) {
        this.points = points;

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        } else {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }

        if (caller instanceof MapsActivity) {
            MapsActivity mapsActivity = (MapsActivity) caller;
            mapsActivity.drawPath();
            mapsActivity.updateScore();
        }
    }

    @Override
    public void handlePointsNearLocation(List<DataEntry> entries) {
        Map<String, List<LatLng>> positionsByUser = new HashMap<>();
        for (DataEntry entry: entries) {
            if (entry.getUser() == null || entry.getUser().getId().equals(FirebaseConnection.user)) {
                continue;
            }
            if (entry.getPoints() == null) {
                positionsByUser.put(entry.getUser().getId(), Collections.singletonList(new LatLng(entry.getLatitude(), entry.getLongitude())));
            } else if (!positionsByUser.containsKey(entry.getUser().getId()) || positionsByUser.get(entry.getUser().getId()).size() < entry.getPoints().size()) {
                positionsByUser.put(entry.getUser().getId(), entry.getPoints());
            }
        }
        if (caller instanceof MapsActivity) {
            ((MapsActivity) caller).drawNearbyPolylines(new ArrayList<>(positionsByUser.values()));
        }
    }

    @Override
    public synchronized void removePathTo(LatLng location) {
        List<LatLng> toRemove = new ArrayList<>();
        BigDecimal currentLocationLatitude = BigDecimal.valueOf(location.latitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal currentLocationLongitude = BigDecimal.valueOf(location.longitude).setScale(4, BigDecimal.ROUND_HALF_UP);
        for (LatLng point: points) {
            BigDecimal pointLocationLatitude = BigDecimal.valueOf(point.latitude).setScale(4, BigDecimal.ROUND_HALF_UP);
            BigDecimal pointLocationLongitude = BigDecimal.valueOf(point.longitude).setScale(4, BigDecimal.ROUND_HALF_UP);
            if (currentLocationLatitude.subtract(pointLocationLatitude).abs().compareTo(BigDecimal.valueOf(0.00001)) < 0
                    && currentLocationLongitude.subtract(pointLocationLongitude).abs().compareTo(BigDecimal.valueOf(0.00001)) < 0) {
                toRemove.add(point);
                break;
            }
            toRemove.add(point);
        }
        points.removeAll(toRemove);
        if (caller instanceof MapsActivity) {
            ((MapsActivity) caller).drawPath();
        }
    }

    @Override
    public void handleUsersInfo(List<DataEntry> users) {
    }
}
