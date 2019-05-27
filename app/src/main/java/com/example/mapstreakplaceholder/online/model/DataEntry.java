package com.example.mapstreakplaceholder.online.model;

import com.example.mapstreakplaceholder.utils.ScoreUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataEntry {

    private Double longitude;
    private Double latitude;
    private Long timestamp;
    private User user;
    private List<LatLng> points;
    private Double score;

    public DataEntry() {
    }

    public DataEntry(LatLng location, User user, List<LatLng> points) {
        longitude = location.longitude;
        latitude = location.latitude;
        this.user = user;
        this.points = points;
        score = ScoreUtil.calculateScore(points);
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Exclude
    public Long getTimestampLong() {
        return timestamp;
    }

    public Map<String, String> getTimestamp() {
        return ServerValue.TIMESTAMP;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<Map<String, Double>> pointsHashMap) {
        List<LatLng> points = new ArrayList<>();
        for (Map<String, Double> point: pointsHashMap) {
            if (point.containsKey("latitude") && point.containsKey("longitude")) {
                points.add(new LatLng(point.get("latitude"), point.get("longitude")));
            }
        }
        this.points = points;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
