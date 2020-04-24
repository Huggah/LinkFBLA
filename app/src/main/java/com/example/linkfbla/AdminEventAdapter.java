package com.example.linkfbla;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class AdminEventAdapter extends CompetitiveEventAdapter {

    // Callback listener
    private AdapterEventListener listener;

    // Array that keeps checkmarks in memory
    private boolean[] approved;

    // Interface acts as delegate for lambda function, so click can be handled by activity
    interface AdapterEventListener {
        void onApproveClick(int position, boolean approved);
    }

    public AdminEventAdapter(Context context, ArrayList<CompetitiveEvent> events, AdapterEventListener listener) {
        super(context, events);
        layout = R.layout.admin_view_competitive_event;
        this.listener = listener;
        // Sync array to event data from database
        this.approved = new boolean[events.size()];
        for (int i = 0; i < events.size(); i++) {
            this.approved[i] = events.get(i).approved;
        }
    }

    // Holds view data in memory
    static class ViewHolder extends CompetitiveEventAdapter.ViewHolder {
        CheckBox approve;
        TextView teamMembers;
    }

    // Returns this class's type of viewHolder
    @Override
    protected CompetitiveEventAdapter.ViewHolder getViewHolder() {
        return new ViewHolder();
    }

    @Override
    protected CompetitiveEventAdapter.ViewHolder setupViews(CompetitiveEventAdapter.ViewHolder superHolder, View view) {
        ViewHolder holder = (ViewHolder) super.setupViews(superHolder, view);
        holder.approve = view.findViewById(R.id.approve);
        holder.teamMembers = view.findViewById(R.id.team_members);
        return holder;
    }

    @Override
    protected void convertViews(CompetitiveEventAdapter.ViewHolder superHolder, int position) {
        // Pass through superclass method first, then cast to this class's viewHolder
        super.convertViews(superHolder, position);
        ViewHolder holder = (ViewHolder) superHolder;

        holder.approve.setChecked(approved[position]);
        holder.approve.setOnClickListener(v -> {
            boolean isChecked = holder.approve.isChecked();
            // call lambda
            listener.onApproveClick(position, isChecked);
            // Sync array to check marks
            approved[position] = isChecked;
        });

        // Need access to profile data through activity to list all team members
        HomeActivity activity = HomeActivity.getInstance();
        StringBuilder builder = new StringBuilder();
        ArrayList<String> teamMembers = events.get(position).teamMembers;
        for (int i = 0; i < teamMembers.size(); i++) {
            builder.append(activity.chapter.members.get(teamMembers.get(i)).name);
            if (i < teamMembers.size() - 1) {
                builder.append("\n");
            }
        }
        holder.teamMembers.setText(builder);
    }
}