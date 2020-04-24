package com.example.linkfbla;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventsListActivity extends AppCompatActivity {

    private HomeActivity activity;
    private ArrayList<CompetitiveEvent> events;

    private static final String TAG = "EventsListActivity";

    /**
     * Adds the event ID to competitiveEvents list in a user's profile.
     * @param activity Instance of HomeActivity, for chapter data
     * @param uid The user's uid to add the event to the profile
     * @param eventId The unique ID of the event
     * @param eventName The name of the competitive event, check if user is already doing it
     * @param onSuccessListener Called when event successfully registered to user's profile
     */
    public static void AddEventToProfile(HomeActivity activity, String uid, String eventId, String eventName, OnSuccessListener<Void> onSuccessListener) {
        // Checks if user is already doing this event
        boolean canDoEvent = true;
        UserProfile profile = activity.chapter.members.get(uid);
        assert profile != null;
        for (String ptr : profile.competitiveEvents) {
            CompetitiveEvent check = activity.chapter.competitiveEvents.get(ptr);
            assert check != null;
            if (check.name.equals(eventName)) {
                if (check.teamMembers.contains(uid)) {
                    // Member is already doing event
                    canDoEvent = false;
                    break;
                }
            }
        }
        if (profile.competitiveEvents.size() < 4 && canDoEvent) {
            String profilePath = Chapter.DATABASE_PATH + activity.chapter_name + "/members/" + uid + "/competitiveEvents/" + profile.competitiveEvents.size();
            DatabaseReference profileRef = activity.firebaseDatabase.getReference(profilePath);
            profileRef.setValue(eventId).addOnSuccessListener(onSuccessListener).addOnFailureListener(e -> Log.w(TAG, e));
        } else {
            Toast.makeText(activity, "You cannot have 2 of the same event or more than 4 events.", Toast.LENGTH_SHORT).show();
        }
    }

    // Called when Add Event button is clicked from ListView
    private EventListAdapter.AdapterEventListener listener = i -> {
        String eventId = activity.user.getUid() + events.get(i).name;
        CompetitiveEvent event = events.get(i);

        AddEventToProfile(activity, activity.user.getUid(), eventId, event.name, aVoid -> {
            event.teamMembers.add(activity.user.getUid());
            String path = Chapter.DATABASE_PATH + activity.chapter_name + "/competitiveEvents/" + eventId;
            DatabaseReference ref = activity.firebaseDatabase.getReference(path);
            ref.setValue(event).addOnSuccessListener(aVoid1 -> {
                Toast.makeText(activity, "Added competitive event.", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> Log.w(TAG, e));
        });
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_list);
        activity = HomeActivity.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView eventsListView = findViewById(R.id.competitive_events_list);
        eventsListView.setOnItemClickListener((parent, view1, position, id) -> {
            // URL on the FBLA website is a base URL directory followed by a formatted event name string
            String url = "https://www.fbla-pbl.org/competitive-event/" + events.get(position).name.toLowerCase().replace(' ', '-');
            Intent intent = new Intent(activity, ViewEventActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        });

        DatabaseReference ref = activity.firebaseDatabase.getReference("Events");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events = dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<CompetitiveEvent>>() {});
                EventListAdapter adapter = new EventListAdapter(events, EventsListActivity.this, listener);
                eventsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
