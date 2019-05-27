package com.example.mapstreakplaceholder.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.List;

public class ScoreUtil {

    public static double calculateScore(List<LatLng> points) {
        if (points.isEmpty()) {
            return 0;
        }
        double score = 0;
        LatLng nextPoint = points.get(points.size() - 1);
        LatLng currentPoint;
        for (int i = points.size() - 2; i >= 0; i--) {
            currentPoint = points.get(i);
            score += SphericalUtil.computeDistanceBetween(currentPoint, nextPoint);
            nextPoint = currentPoint;
        }
        return score;
    }
}
