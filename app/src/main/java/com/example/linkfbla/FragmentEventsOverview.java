package com.example.linkfbla;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentEventsOverview extends Fragment {
    private HomeActivity activity;
    private ArrayList<String> eventIdsList;

    private static final String TAG = "FragmentEventsOverview";

    // Called when admin clicks check box on listView to approve or disapprove
    private AdminEventAdapter.AdapterEventListener listener = (position, approved) -> {
        // Change value in database to value in check box
        String path = Chapter.DATABASE_PATH + activity.chapter_name + "/competitiveEvents/" + eventIdsList.get(position) + "/approved";
        DatabaseReference ref = activity.firebaseDatabase.getReference(path);
        ref.setValue(approved).addOnFailureListener(e -> Log.w(TAG, e));
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_overview, container, false);
        activity = (HomeActivity) getActivity();

        ListView eventsListView = view.findViewById(R.id.events_overview_list);
        TextView noEvents = view.findViewById(R.id.no_events_text);

        ArrayList<CompetitiveEvent> events = new ArrayList<>(activity.chapter.competitiveEvents.values());
        eventIdsList = new ArrayList<>(activity.chapter.competitiveEvents.keySet());

        if (events.size() > 0) {
            // Get rid of no events text
            eventsListView.setVisibility(View.VISIBLE);
            noEvents.setVisibility(View.GONE);

            // Setup listView
            AdminEventAdapter adapter = new AdminEventAdapter(activity, events, listener);
            eventsListView.setAdapter(adapter);
        }

        return view;
    }
}
