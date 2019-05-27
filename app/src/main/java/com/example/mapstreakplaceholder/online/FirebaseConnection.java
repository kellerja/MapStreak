package com.example.mapstreakplaceholder.online;

import com.example.mapstreakplaceholder.online.model.DataEntry;
import com.example.mapstreakplaceholder.online.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FirebaseConnection implements DatabaseConnection {

    private DatabaseReference mapReference;
    private DatabaseReference userReference;
    public static String user;
    private ResultHandler resultHandler;

    public FirebaseConnection(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mapReference = database.getReference("map");
        userReference = database.getReference("user");
        Random random = new Random();
        user = User.getCurrentUser() == null || User.getCurrentUser().getId() == null ? "Guest" + random.nextInt() : User.getCurrentUser().getId();
        notifyUserOfLineIntersection();
    }

    private String convertLatLngToKey(LatLng latLng) {
        String key = String.format(Locale.ENGLISH, "%s%07.04f;%s%08.04f", latLng.latitude < 0 ? "" : "+", latLng.latitude, latLng.longitude < 0 ? "" : "+", latLng.longitude);
        /*key = key.replaceAll("0", "A");
        key = key.replaceAll("1", "B");
        key = key.replaceAll("2", "C");
        key = key.replaceAll("3", "D");
        key = key.replaceAll("4", "E");
        key = key.replaceAll("5", "F");
        key = key.replaceAll("6", "G");
        key = key.replaceAll("7", "H");
        key = key.replaceAll("8", "I");
        key = key.replaceAll("9", "J");*/
        key = key.replaceAll("\\.", "");
        return key;
    }

    @Override
    public void saveLocation(final LatLng location, final List<LatLng> previousPoints) {
        final String[] key = convertLatLngToKey(location).split(";");
        mapReference
                .child(Character.toString(key[0].charAt(0)) + Character.toString(key[1].charAt(0)))
                .child(key[0].substring(1, 3) + key[1].substring(1, 4))
                .child(key[0].substring(3, 5) + key[1].substring(4, 6))
                .child(key[0].substring(5, 7) + key[1].substring(6, 8))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    DataEntry entry = dataSnapshot.getValue(DataEntry.class);
                    if (entry.getUser().getId().equals(user)) {
                        return;
                    }
                    userReference.child(entry.getUser().getId()).child("removeToAndIncluding").setValue(location);
                    for (LatLng point: entry.getPoints()) {
                        String[] pointKey = convertLatLngToKey(point).split(";");
                        mapReference
                                .child(Character.toString(pointKey[0].charAt(0)) + Character.toString(pointKey[1].charAt(0)))
                                .child(pointKey[0].substring(1, 3) + pointKey[1].substring(1, 4))
                                .child(pointKey[0].substring(3, 5) + pointKey[1].substring(4, 6))
                                .child(pointKey[0].substring(5, 7) + pointKey[1].substring(6, 8)).removeValue();
                    }
                    removeOtherUserPoints(entry, location);
                }
                DataEntry entry = new DataEntry(location, User.getCurrentUser(), previousPoints);
                mapReference
                        .child(Character.toString(key[0].charAt(0)) + Character.toString(key[1].charAt(0)))
                        .child(key[0].substring(1, 3) + key[1].substring(1, 4))
                        .child(key[0].substring(3, 5) + key[1].substring(4, 6))
                        .child(key[0].substring(5, 7) + key[1].substring(6, 8)).setValue(entry);
                userReference.child(user).child("lastPoint").setValue(entry);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("ERROR SAVING POSITION: " + databaseError.getMessage());
            }
        });
    }

    public void saveLocationLowPriority(final List<LatLng> pointsToAdd, final List<LatLng> previousPoints, final List<LatLng> removed, final LatLng lastLocation) {
        final LatLng location = pointsToAdd.remove(0);
        removed.add(location);
        final String[] key = convertLatLngToKey(location).split(";");
        mapReference
                .child(Character.toString(key[0].charAt(0)) + Character.toString(key[1].charAt(0)))
                .child(key[0].substring(1, 3) + key[1].substring(1, 4))
                .child(key[0].substring(3, 5) + key[1].substring(4, 6))
                .child(key[0].substring(5, 7) + key[1].substring(6, 8))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean wasIntercepted = false;
                        if (dataSnapshot.getValue() != null) {
                            DataEntry entry = dataSnapshot.getValue(DataEntry.class);
                            if (!entry.getUser().getId().equals(user)) {
                                handleIntersection(location, previousPoints, removed);
                                wasIntercepted = true;
                            }
                        } else {
                            updateDatabaseMapWithPoint(previousPoints, removed, location, key);
                        }
                        if (!pointsToAdd.isEmpty()) {
                            saveLocationLowPriority(pointsToAdd, previousPoints, removed, lastLocation);
                        }
                        if (previousPoints.isEmpty() && pointsToAdd.isEmpty() && !wasIntercepted) {
                            alertFinalPointOfIntersection(removed, lastLocation);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("ERROR SAVING POSITION: " + databaseError.getMessage());
                    }
                });
    }

    private void updateDatabaseMapWithPoint(List<LatLng> previousPoints, List<LatLng> removed, LatLng location, String[] key) {
        List<LatLng> temp = new ArrayList<>(previousPoints);
        temp.addAll(removed);
        DataEntry entry = new DataEntry(location, User.getCurrentUser(), temp);
        mapReference
                .child(Character.toString(key[0].charAt(0)) + Character.toString(key[1].charAt(0)))
                .child(key[0].substring(1, 3) + key[1].substring(1, 4))
                .child(key[0].substring(3, 5) + key[1].substring(4, 6))
                .child(key[0].substring(5, 7) + key[1].substring(6, 8)).setValue(entry);
    }

    private void handleIntersection(LatLng location, List<LatLng> previousPoints, List<LatLng> removed) {
        userReference.child(user).child("removeToAndIncluding").setValue(location);
        for (LatLng point: previousPoints) {
            String[] pointKey = convertLatLngToKey(point).split(";");
            mapReference
                    .child(Character.toString(pointKey[0].charAt(0)) + Character.toString(pointKey[1].charAt(0)))
                    .child(pointKey[0].substring(1, 3) + pointKey[1].substring(1, 4))
                    .child(pointKey[0].substring(3, 5) + pointKey[1].substring(4, 6))
                    .child(pointKey[0].substring(5, 7) + pointKey[1].substring(6, 8)).removeValue();
        }
        previousPoints.clear();
        removed.remove(location);
        for (LatLng point: removed) {
            String[] pointKey = convertLatLngToKey(point).split(";");
            mapReference
                    .child(Character.toString(pointKey[0].charAt(0)) + Character.toString(pointKey[1].charAt(0)))
                    .child(pointKey[0].substring(1, 3) + pointKey[1].substring(1, 4))
                    .child(pointKey[0].substring(3, 5) + pointKey[1].substring(4, 6))
                    .child(pointKey[0].substring(5, 7) + pointKey[1].substring(6, 8)).removeValue();
        }
        removed.clear();
    }

    private void alertFinalPointOfIntersection(List<LatLng> removed, LatLng lastLocation) {
        List<LatLng> temp = new ArrayList<>(removed);
        temp.add(lastLocation);
        DataEntry entry = new DataEntry(lastLocation, User.getCurrentUser(), temp);
        String[] keyLastLocation = convertLatLngToKey(lastLocation).split(";");
        mapReference
                .child(Character.toString(keyLastLocation[0].charAt(0)) + Character.toString(keyLastLocation[1].charAt(0)))
                .child(keyLastLocation[0].substring(1, 3) + keyLastLocation[1].substring(1, 4))
                .child(keyLastLocation[0].substring(3, 5) + keyLastLocation[1].substring(4, 6))
                .child(keyLastLocation[0].substring(5, 7) + keyLastLocation[1].substring(6, 8)).setValue(entry);
    }

    private void removeOtherUserPoints(final DataEntry entry, final LatLng location) {
        userReference.child(entry.getUser().getId()).child("lastPoint").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    DataEntry dataEntry = dataSnapshot.getValue(DataEntry.class);
                    List<LatLng> newPoints = new ArrayList<>();
                    for (int i = dataEntry.getPoints().size() - 1; i >= 0; i--) {
                        LatLng point = dataEntry.getPoints().get(i);
                        if (location.equals(point)) {
                            break;
                        }
                        newPoints.add(0, point);
                    }
                    for (int i = 0; i < newPoints.size(); i++) {
                        LatLng point = newPoints.get(i);
                        String[] key = convertLatLngToKey(point).split(";");
                        mapReference
                                .child(Character.toString(key[0].charAt(0)) + Character.toString(key[1].charAt(0)))
                                .child(key[0].substring(1, 3) + key[1].substring(1, 4))
                                .child(key[0].substring(3, 5) + key[1].substring(4, 6))
                                .child(key[0].substring(5, 7) + key[1].substring(6, 8))
                                .child("points").setValue(newPoints.subList(0, i + 1));
                    }
                    if (newPoints.isEmpty()) {
                        userReference.child(entry.getUser().getId()).child("lastPoint").removeValue();
                    } else {
                        DataEntry newDataEntry = new DataEntry(newPoints.get(newPoints.size() - 1), entry.getUser(), newPoints);
                        userReference.child(entry.getUser().getId()).child("lastPoint").setValue(newDataEntry);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void getPointsNear(final LatLng location) {
        String[] key = convertLatLngToKey(location).split(";");
        mapReference
                .child(Character.toString(key[0].charAt(0)) + Character.toString(key[1].charAt(0)))
                .child(key[0].substring(1, 3) + key[1].substring(1, 4))
                .child(key[0].substring(3, 5) + key[1].substring(4, 6))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<DataEntry> entries = new ArrayList<>();
                        for (DataSnapshot data: dataSnapshot.getChildren()) {
                            DataEntry entry = data.getValue(DataEntry.class);
                            entries.add(entry);
                        }
                        resultHandler.handlePointsNearLocation(entries);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    @Override
    public void getPointsForUser() {
        userReference.child(user).child("lastPoint").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LatLng> points = new ArrayList<>();
                if (dataSnapshot.getValue() != null) {
                    DataEntry entry = dataSnapshot.getValue(DataEntry.class);
                    points = entry.getPoints();
                    if (points == null) {
                        points = new ArrayList<>();
                    }
                    points.add(new LatLng(entry.getLatitude(), entry.getLongitude()));
                }
                resultHandler.handlePointsByUser(points);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Could not get points near user with error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void getAllUsersInfo() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DataEntry> usersInfo = new ArrayList<>();
                for (DataSnapshot usersSnapshot: dataSnapshot.getChildren()) {
                    if (usersSnapshot.hasChild("lastPoint") && usersSnapshot.child("lastPoint").getValue() != null) {
                        usersInfo.add(usersSnapshot.child("lastPoint").getValue(DataEntry.class));
                    }
                }
                resultHandler.handleUsersInfo(usersInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setResultHandler(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
    }

    private void notifyUserOfLineIntersection() {
        userReference.child(user)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.hasChild("longitude") && dataSnapshot.hasChild("latitude") && "removeToAndIncluding".equals(dataSnapshot.getKey())) {
                    LatLng entry = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                    resultHandler.removePathTo(entry);
                }
                userReference.child(user).child("removeToAndIncluding").removeValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
