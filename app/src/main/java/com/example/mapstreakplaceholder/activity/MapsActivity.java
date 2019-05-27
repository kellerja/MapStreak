package com.example.mapstreakplaceholder.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mapstreakplaceholder.R;
import com.example.mapstreakplaceholder.adapter.MenuAdapter;
import com.example.mapstreakplaceholder.location.LocationServiceManager;
import com.example.mapstreakplaceholder.permissions.Permissions;
import com.example.mapstreakplaceholder.utils.ScoreUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationServiceManager locationManager;
    private Marker currentPositionMarker;
    private Polyline myPathPolyline;
    private List<Polyline> nearbyPathPolylines = new ArrayList<>();
    private Bitmap head;
    private Bitmap tail;

    private TextView myPathScoreTextView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (locationManager == null) {
            locationManager = new LocationServiceManager(this);
        }
        head = BitmapFactory.decodeResource(getResources(), R.drawable.snakehead);
        tail = BitmapFactory.decodeResource(getResources(), R.drawable.snaketail);

        myPathScoreTextView = (TextView) findViewById(R.id.maps_length_score_textview);
        drawerLayout = (DrawerLayout) findViewById(R.id.maps_drawer_layout);

        ListView drawerListView = (ListView) findViewById(R.id.maps_left_drawer_listView);
        drawerListView.setAdapter(new MenuAdapter(this, R.layout.maps_drawer_list_item, R.id.maps_drawer_item_textView));
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (MenuAdapter.getMenuItems()[position]) {
                    case "Profile":
                        intent = new Intent(getBaseContext(), ProfileActivity.class);
                        break;
                    case "High scores":
                        intent = new Intent(getBaseContext(), HighScoresActivity.class);
                        break;
                    case "Logout":
                        intent = new Intent(getBaseContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        FirebaseAuth.getInstance().signOut();
                        break;
                    default:
                        intent = new Intent(getBaseContext(), MapsActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    private Bitmap bitmapFromDrawableVector(Drawable drawable) {
        //TODO: make bitmap image instead
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }

    private Bitmap rotateBitmap(Bitmap originalBitmap, float rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
    }

    public void menuButtonClick(View view) {
        drawerLayout.openDrawer(Gravity.START, true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        //mMap.getUiSettings().setRotateGesturesEnabled(false);
        /*
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(0, 0))
                .tilt(45)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        */
    }

    @Override
    protected void onStart() {
        locationManager.startGoogleApiClient();
        super.onStart();
        locationManager.startLocationUpdates();
    }

    @Override
    protected void onStop() {
        locationManager.stopGoogleApiClient();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == Permissions.REQUEST_LOCATION_PERMISSIONS.getPermissionCode()) {
            if (isLocationPermissionGranted(grantResults)) {
                Location mLastLocation = locationManager.getLastLocation();
                locationManager.startLocationUpdates();
                updateMap(mLastLocation);
            }
        }
    }

    private boolean isLocationPermissionGranted(@NonNull int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    public void updateMap(Location location) {
        if (location != null && mMap != null) {

            if (currentPositionMarker != null) {
                currentPositionMarker.remove();
            }

            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            drawPath();
            if (locationManager.getPoints() == null || locationManager.getPoints().size() < 2) {
                MarkerOptions markerOptions = new MarkerOptions().position(currentLocation).title("Current location");
                currentPositionMarker = mMap.addMarker(markerOptions);
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.coiledsnake);
                Bitmap b = Bitmap.createScaledBitmap(bmp, 180, 180, false);
                currentPositionMarker.setIcon(BitmapDescriptorFactory.fromBitmap(b));
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLocation)
                    .zoom(18.0f)
                    .bearing(location.getBearing())
                    .build();
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            updateScore();
        }
    }

    public void updateScore() {
        if (locationManager.getPoints() == null) {
            return;
        }
        myPathScoreTextView.setText(String.format(Locale.ENGLISH, "Current score: %.4f", ScoreUtil.calculateScore(locationManager.getPoints())));
    }

    public void drawPath() {
        List<LatLng> points = locationManager.getPoints();

        if (points == null) {
            return;
        }

        if (myPathPolyline == null) {
            PolylineOptions polylineOptions = new PolylineOptions().width(40).color(Color.BLUE);
            polylineOptions.addAll(points);

            myPathPolyline = mMap.addPolyline(polylineOptions);
            myPathPolyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromBitmap(head), 350));
            myPathPolyline.setStartCap(new CustomCap(BitmapDescriptorFactory.fromBitmap(tail), 350));
        } else {
            myPathPolyline.setPoints(points);
        }
    }

    public void drawNearbyPolylines(List<List<LatLng>> polylinesToDraw) {
        for (Polyline polyline: nearbyPathPolylines) {
            polyline.remove();
        }
        nearbyPathPolylines = new ArrayList<>();

        for (List<LatLng> points: polylinesToDraw) {
            PolylineOptions polylineOptions = new PolylineOptions().width(40).color(Color.GREEN);
            polylineOptions.addAll(points);
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromBitmap(head), 350));
            polyline.setStartCap(new CustomCap(BitmapDescriptorFactory.fromBitmap(tail), 350));
            nearbyPathPolylines.add(polyline);
        }
    }
}
