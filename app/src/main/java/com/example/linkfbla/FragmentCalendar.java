package com.example.linkfbla;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentCalendar extends Fragment {

    private HomeActivity activity;
    private boolean canAddEvent;
    private ImageView post;
    private String dateSelected;
    private ListView eventsListView;

    private static final String TAG = "FragmentCalendar";

    static void addEvent(HomeActivity activity, String date, String text, OnSuccessListener<Void> listener) {
        String path = Chapter.DATABASE_PATH + activity.chapter_name + "/upcomingEvents/" + date + "/";
        ArrayList<String> values = activity.chapter.upcomingEvents.get(date);
        path += values == null ? 0 : values.size();
        DatabaseReference ref = activity.firebaseDatabase.getReference(path);
        ref.setValue(text).addOnSuccessListener(listener).addOnFailureListener(e -> Log.w(TAG, e));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (HomeActivity)getActivity();
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        final CalendarView calendar = view.findViewById(R.id.calendar);
        eventsListView = view.findViewById(R.id.upcoming_events_list);

        calendar.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            dateSelected = (month + 1) + "-" + dayOfMonth + "-" + year;
            refresh();
        });
        calendar.setDate(new Date().getTime());

        Calendar cal = Calendar.getInstance();
        dateSelected = (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.YEAR);

        if (activity.chapter.adminPermissions.containsKey(activity.user.getUid())) {
            View addEvent = view.findViewById(R.id.add_event);
            addEvent.setVisibility(View.VISIBLE);
            post = view.findViewById(R.id.post);
            final EditText content = view.findViewById(R.id.content);

            // Trigger updating add event icon enabled state after typing post.
            content.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    canAddEvent = !content.getText().toString().isEmpty();
                    updateAddEventIcon();
                }
            });

            post.setOnClickListener(v -> {
                if (canAddEvent) {
                    addEvent(activity, dateSelected, content.getText().toString(), aVoid -> {
                        refresh();
                        content.setText("");
                        updateAddEventIcon();
                    });
                }
            });

            eventsListView.setOnItemClickListener((parent, view1, position, id) -> {
                ConfirmDialog dialog = new ConfirmDialog("Remove event", "Remove calendar event?", () -> {
                    ArrayList<String> events = activity.chapter.upcomingEvents.get(dateSelected);
                    if (events == null) return;
                    events.remove(position);
                    String path = Chapter.DATABASE_PATH + activity.chapter_name + "/upcomingEvents/" + dateSelected;
                    DatabaseReference ref = activity.firebaseDatabase.getReference(path);
                    ref.setValue(events).addOnSuccessListener(aVoid -> {
                        refresh();
                    }).addOnFailureListener(e -> Log.w(TAG, e));
                });
                dialog.show(activity.getSupportFragmentManager(), "remove upcoming event dialog");
            });
        }

        return view;
    }

    private void refresh() {
        ArrayList<String> events = activity.chapter.upcomingEvents.get(dateSelected);
        if (events != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, events);
            eventsListView.setVisibility(View.VISIBLE);
            eventsListView.setAdapter(adapter);
        } else {
            eventsListView.setVisibility(View.GONE);
        }
    }

    private void updateAddEventIcon() {
        if (canAddEvent) {
            post.setImageResource(R.drawable.ic_add_enabled);
        } else {
            post.setImageResource(R.drawable.ic_add);
        }
    }
}
