package com.example.linkfbla;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyEventAdapter extends CompetitiveEventAdapter {

    private AdapterEventListener listener;
    private String user;

    // Delegate for callbacks so activity can handle events
    interface AdapterEventListener {
        void onRemoveClick(int position);
        void onAddTeamMemberClick(int position);
    }

    public MyEventAdapter(Context context, ArrayList<CompetitiveEvent> events, AdapterEventListener listener, String user) {
        super(context, events);
        layout = R.layout.my_competitive_event;
        this.listener = listener;
        this.user = user;
    }

    // Add these views to base event adapter's viewHolder
    static class ViewHolder extends CompetitiveEventAdapter.ViewHolder {
        View addTeamMember;
        ImageView remove;
        TextView teamMembers;
        View approved;
    }

    // Return this class's viewHolder
    @Override
    protected CompetitiveEventAdapter.ViewHolder getViewHolder() {
        return new ViewHolder();
    }

    @Override
    protected CompetitiveEventAdapter.ViewHolder setupViews(CompetitiveEventAdapter.ViewHolder superHolder, View view) {
        ViewHolder holder = (ViewHolder) super.setupViews(superHolder, view);
        holder.addTeamMember = view.findViewById(R.id.add_team_member);
        holder.remove = view.findViewById(R.id.remove);
        holder.teamMembers = view.findViewById(R.id.team_members);
        holder.approved = view.findViewById(R.id.approved);
        return holder;
    }

    @Override
    protected void convertViews(CompetitiveEventAdapter.ViewHolder superHolder, int position) {
        super.convertViews(superHolder, position);
        ViewHolder holder = (ViewHolder) superHolder;
        // If approved, can't remove, and show message that event has been approved.
        if (events.get(position).approved) {
            holder.remove.setVisibility(View.GONE);
            holder.remove.setClickable(false);
            holder.approved.setVisibility(View.VISIBLE);
        } else {
            holder.remove.setVisibility(View.VISIBLE);
            holder.remove.setOnClickListener(v -> listener.onRemoveClick(position));
            holder.approved.setVisibility(View.INVISIBLE);
        }

        if (events.get(position).team) {
            // Event is a team event
            if (events.get(position).approved) {
                // Event is approved, cannot add team members
                holder.addTeamMember.setVisibility(View.GONE);
                holder.addTeamMember.setClickable(false);
            }
            else {
                // Event isn't approved, can add team members
                holder.addTeamMember.setVisibility(View.VISIBLE);
                holder.addTeamMember.setOnClickListener(v -> listener.onAddTeamMemberClick(position));
            }

            // If user is only team member, don't display text view
            if (events.get(position).teamMembers.size() > 1) {
                holder.teamMembers.setVisibility(View.VISIBLE);
            } else {
                holder.teamMembers.setVisibility(View.GONE);
            }
            // Requires database to fetch names using UIDs in teamMembers list
            HomeActivity activity = HomeActivity.getInstance();
            StringBuilder builder = new StringBuilder();
            ArrayList<String> teamMembers = events.get(position).teamMembers;
            for (int i = 0; i < teamMembers.size(); i++) {
                if (!(teamMembers.get(i).equals(user)) && activity.chapter.members.get(teamMembers.get(i)) != null) {
                    if (!builder.toString().isEmpty()) {
                        // Add a new line for each team member
                        builder.append("\n");
                    }
                    // Add a team member's name if it is not own name
                    builder.append(activity.chapter.members.get(teamMembers.get(i)).name);
                }
            }
            holder.teamMembers.setText(builder);
        } else {
            holder.addTeamMember.setVisibility(View.GONE);
            holder.teamMembers.setVisibility(View.GONE);
        }

    }
}
