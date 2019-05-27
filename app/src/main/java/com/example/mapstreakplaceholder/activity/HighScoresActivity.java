package com.example.mapstreakplaceholder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mapstreakplaceholder.R;
import com.example.mapstreakplaceholder.adapter.MenuAdapter;
import com.example.mapstreakplaceholder.online.DatabaseConnection;
import com.example.mapstreakplaceholder.online.FirebaseConnection;
import com.example.mapstreakplaceholder.online.ResultHandler;
import com.example.mapstreakplaceholder.online.model.DataEntry;
import com.example.mapstreakplaceholder.online.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HighScoresActivity extends AppCompatActivity implements ResultHandler {

    private ListView highScoresListView;
    private DatabaseConnection databaseConnection;
    private DrawerLayout drawerLayout;
    private int playerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseConnection = new FirebaseConnection(this);

        databaseConnection.getAllUsersInfo();

        highScoresListView = (ListView) findViewById(R.id.highscores_scores_listView);
        drawerLayout = (DrawerLayout) findViewById(R.id.highscores_drawer_layout);

        ListView drawerListView = (ListView) findViewById(R.id.highscores_left_drawer_listView);
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

    public void menuButtonClick(View view) {
        drawerLayout.openDrawer(Gravity.START, true);
    }

    @Override
    public void handlePointsByUser(List<LatLng> points) {
    }

    @Override
    public void handlePointsNearLocation(List<DataEntry> entry) {
    }

    @Override
    public void removePathTo(LatLng location) {
    }

    @Override
    public void handleUsersInfo(List<DataEntry> users) {
        List<String> scores = new ArrayList<>();
        User currentUser = User.getCurrentUser();
        Collections.sort(users, new Comparator<DataEntry>() {
            @Override
            public int compare(DataEntry o1, DataEntry o2) {
                return o1.getScore().compareTo(o2.getScore());
            }
        });
        for (DataEntry dataEntry: users) {
            if (currentUser != null && currentUser.getId() != null && dataEntry.getUser().getId().equals(currentUser.getId())) {
                playerPosition = users.indexOf(dataEntry);
            }
            scores.add(String.format(Locale.ENGLISH, "%s has a length of %.4f meters", dataEntry.getUser().getName(), dataEntry.getScore()));
        }
        highScoresListView.setAdapter(new ArrayAdapter<>(this, R.layout.highscores_list_item, R.id.highscores_item_textView, scores));
        highScoresListView.setSelection(playerPosition);
    }
}
