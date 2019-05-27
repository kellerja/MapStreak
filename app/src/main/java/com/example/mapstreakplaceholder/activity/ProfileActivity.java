package com.example.mapstreakplaceholder.activity;

import android.content.Intent;
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
import com.example.mapstreakplaceholder.online.model.User;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileActivity extends AppCompatActivity {

    private TextView idTextView;
    private TextView nameTextView;
    private TextView emailTextView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        idTextView = (TextView) findViewById(R.id.profile_id_textView);
        nameTextView = (TextView) findViewById(R.id.profile_name_textView);
        emailTextView = (TextView) findViewById(R.id.profile_email_textView);
        drawerLayout = (DrawerLayout) findViewById(R.id.profile_drawer_layout);

        User user = User.getCurrentUser();

        if (user != null) {
            if (user.getId() != null) {
                idTextView.setText(user.getId());
            }
            if (user.getEmail() != null) {
                emailTextView.setText(user.getEmail());
            } else {
                emailTextView.setText("No email");
            }
            if (user.getName() != null) {
                nameTextView.setText(user.getName());
            } else {
                nameTextView.setText("Unnamed");
            }
        }

        ListView drawerListView = (ListView) findViewById(R.id.profile_left_drawer_listView);
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

}
