package com.example.linkfbla;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class EventListAdapter extends CompetitiveEventAdapter {

    // Delegate for lambda so activity can handle adding event
    public interface AdapterEventListener {
        void onClickAddEvent(int i);
    }

    // Callback Listener
    private AdapterEventListener listener;

    public EventListAdapter(ArrayList<CompetitiveEvent> events, Context context, AdapterEventListener listener) {
        super(context, events);
        layout = R.layout.add_competitive_event;
        this.listener = listener;
    }

    // This class's viewHolder
    static class ViewHolder extends CompetitiveEventAdapter.ViewHolder {
        Button addEvent;
    }

    // Return this class's viewHolder
    @Override
    protected CompetitiveEventAdapter.ViewHolder getViewHolder() {
        return new ViewHolder();
    }

    @Override
    protected CompetitiveEventAdapter.ViewHolder setupViews(CompetitiveEventAdapter.ViewHolder superHolder, View view) {
        ViewHolder holder = (ViewHolder) super.setupViews(superHolder, view);
        holder.addEvent = view.findViewById(R.id.add_to_events);
        return holder;
    }

    @Override
    protected void convertViews(CompetitiveEventAdapter.ViewHolder superHolder, int position) {
        // Pass through superclass method first, then cast to this class's viewHolder
        super.convertViews(superHolder, position);
        ViewHolder holder = (ViewHolder) superHolder;
        holder.addEvent.setOnClickListener(v -> listener.onClickAddEvent(position));
    }
}
