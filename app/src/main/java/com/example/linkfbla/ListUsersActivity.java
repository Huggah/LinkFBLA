package com.example.linkfbla;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        // Unpack intent from calling activity.
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        final ArrayList<String> uids = data.getStringArrayList("Uids");
        if (uids == null) return;

        // Get profiles from UIDs using database
        HomeActivity activity = HomeActivity.getInstance();
        ArrayList<UserProfile> profiles = new ArrayList<>();
        for (String uid : uids) {
            profiles.add(activity.chapter.members.get(uid));
        }

        // Setup listView
        ListView listView = findViewById(R.id.list_users);
        ListUsersAdapter adapter = new ListUsersAdapter(this, profiles);
        if (getCallingActivity() != null) {
            // This activity is called for result to select a user
            listView.setOnItemClickListener((parent, view, position, id) -> {
                // User selected, set result to their UID and finish
                int i = adapter.getCount() - position - 1;
                Intent resultIntent = new Intent();
                resultIntent.putExtra("uid", uids.get(i));
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }
        listView.setAdapter(adapter);

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Back button logic
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
