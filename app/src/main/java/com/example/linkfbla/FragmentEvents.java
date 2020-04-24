package com.example.linkfbla;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentEvents extends Fragment {
    private HomeActivity activity;
    private TextView noEvents;
    private ListView myEventsList;

    private static final String TAG = "FragmentEvents";

    private MyEventAdapter.AdapterEventListener listener = new MyEventAdapter.AdapterEventListener() {
        // Edits two places in database: removing reference to event from user, and removing user from event,
        // or if there is only one team member, removing the event.
        @Override
        public void onRemoveClick(int position) {
            // Called when removed event from list, removes reference to event from user profile
            OnSuccessListener<Void> removeFromProfile = aVoid -> {
                String profilePath = Chapter.DATABASE_PATH + activity.chapter_name + "/members/" + activity.user.getUid() + "/competitiveEvents";
                DatabaseReference profileRef = activity.firebaseDatabase.getReference(profilePath);

                // Removes reference at position from list, sets as new events list in database
                ArrayList<String> eventIds = activity.chapter.members.get(activity.user.getUid()).competitiveEvents;
                eventIds.remove(position);

                profileRef.setValue(eventIds).addOnSuccessListener(aVoid1 -> refreshPage()).addOnFailureListener(e -> Log.w(TAG, e));
            };
            ConfirmDialog dialog = new ConfirmDialog("Remove Event", "Are you sure you want to remove this event?", () -> {
                String eventId = activity.chapter.members.get(activity.user.getUid()).competitiveEvents.get(position);
                String path = Chapter.DATABASE_PATH + activity.chapter_name + "/competitiveEvents/" + eventId;
                DatabaseReference ref = activity.firebaseDatabase.getReference(path);
                if (activity.chapter.competitiveEvents.get(eventId).teamMembers.size() != 1) {
                    // More than 1 team member, only remove self from members list
                    ref = ref.child("teamMembers");
                    ArrayList<String> members = activity.chapter.competitiveEvents.get(eventId).teamMembers;
                    members.remove(activity.user.getUid());
                    ref.setValue(members).addOnSuccessListener(removeFromProfile).addOnFailureListener(e -> Log.w(TAG, e));
                } else {
                    // Only team member, remove entire event
                    ref.setValue(null).addOnSuccessListener(removeFromProfile).addOnFailureListener(e -> Log.w(TAG, e));
                }
            });
            dialog.show(activity.getSupportFragmentManager(), "remove event dialog");
        }

        // Opens ListUsersActivity
        @Override
        public void onAddTeamMemberClick(int position) {
            Intent intent = new Intent(activity, ListUsersActivity.class);
            ArrayList<String> uids = new ArrayList<>();
            String[] membersList = activity.chapter.members.keySet().toArray(new String[0]);
            for (String uid : membersList) {
                if (!(uid.equals(activity.user.getUid()) ||
                        (activity.chapter.adminPermissions.get(uid) != null && activity.chapter.adminPermissions.get(uid).equals("advisor")))) {
                    // Don't include advisors (not FBLA members) and self in list, invalid team members
                    uids.add(uid);
                }
            }
            intent.putExtra("Uids", uids);
            startActivityForResult(intent, position);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        activity = (HomeActivity)getActivity();

        noEvents = view.findViewById(R.id.no_events_text);
        myEventsList = view.findViewById(R.id.my_events_list);

        View addEvent = view.findViewById(R.id.add_competitive_event);
        addEvent.setOnClickListener(v -> startActivity(new Intent(activity, EventsListActivity.class)));

        ImageView refresh = view.findViewById(R.id.refresh);
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.spin);
        refresh.setOnClickListener(v -> {
            refresh.startAnimation(anim);
            refreshPage();
        });
        refreshPage();

        return view;
    }

    private void refreshPage() {
        if (activity.chapter.members.get(activity.user.getUid()) == null) return;

        if (activity.chapter.members.get(activity.user.getUid()).competitiveEvents.size() > 0) {
            ArrayList<CompetitiveEvent> events = new ArrayList<>();
            myEventsList.setVisibility(View.VISIBLE);
            noEvents.setVisibility(View.GONE);
            for (String eventId : activity.chapter.members.get(activity.user.getUid()).competitiveEvents) {
                events.add(activity.chapter.competitiveEvents.get(eventId));
            }
            MyEventAdapter adapter = new MyEventAdapter(activity, events, listener, activity.user.getUid());
            myEventsList.setAdapter(adapter);
        } else {
            myEventsList.setVisibility(View.GONE);
            noEvents.setVisibility(View.VISIBLE);
        }
    }

    // Finished selecting user
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode >= 0 && requestCode <= 3 && resultCode == RESULT_OK) {
            // Add team member to competitive event, and
            // add reference to competitive event in team member's user profile
            Bundle extras = data.getExtras();
            String uid = extras.getString("uid");

            // Event ID unique to that event in chapter instance
            String eventId = activity.chapter.members.get(activity.user.getUid()).competitiveEvents.get(requestCode);

            EventsListActivity.AddEventToProfile(activity, uid, eventId, activity.chapter.competitiveEvents.get(eventId).name, aVoid -> {
                // To add a team member to list in database, reference must include size of team members list
                String path = Chapter.DATABASE_PATH + activity.chapter_name + "/competitiveEvents/" + eventId + "/teamMembers/" + activity.chapter.competitiveEvents.get(eventId).teamMembers.size();
                DatabaseReference eventRef = activity.firebaseDatabase.getReference(path);
                eventRef.setValue(uid).addOnSuccessListener(aVoid1 -> {
                    refreshPage();
                }).addOnFailureListener(e -> Log.w(TAG, e));
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}