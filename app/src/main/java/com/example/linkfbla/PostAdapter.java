package com.example.linkfbla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class PostAdapter extends BaseAdapter {

    private Context context;
    private Chapter chapter;
    private String uid;
    private AdapterEventListener listener;

    // Delegate for events that need to be handled by activity
    public interface AdapterEventListener {
        void onCheckIn(Button button, int i);
        void onViewMembers(int i);
        void onRemove(int i);
    }

    // Holds views in memory
    static class ViewHolder {
        TextView name;
        ImageView profile_picture;
        TextView content;
        ImageView image;
        TextView date;
        ImageView removePost;
        View meeting;
        TextView meetingDate;
        Button checkIn;
        Button viewAttendees;
    }

    PostAdapter(Context context, Chapter chapter, String uid, AdapterEventListener listener) {
        this.context = context;
        this.chapter = chapter;
        this.uid = uid;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return chapter.posts.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int i = chapter.posts.size() - position - 1;

        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.post, parent, false);
            holder.profile_picture = convertView.findViewById(R.id.post_profile_pic);
            holder.name = convertView.findViewById(R.id.post_name);
            holder.content = convertView.findViewById(R.id.post_content);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.date = convertView.findViewById(R.id.post_date);
            holder.removePost = convertView.findViewById(R.id.post_remove);
            holder.meeting = convertView.findViewById(R.id.post_meeting);
            holder.meetingDate = convertView.findViewById(R.id.post_meeting_title);
            holder.checkIn = convertView.findViewById(R.id.post_check_in);
            holder.viewAttendees = convertView.findViewById(R.id.post_view_attendees);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserProfile author = chapter.members.get(chapter.posts.get(i).author);
        if (author != null) {
            // Set profile picture
            if (author.photoUrl != null) {
                Glide.with(context).load(author.photoUrl)
                        .centerCrop().apply(RequestOptions.circleCropTransform()).into(holder.profile_picture);
            } else {
                holder.profile_picture.setImageResource(R.drawable.ic_linkfbla_icon);
            }
            // Set name
            holder.name.setText(author.name);
        }
        if (chapter.posts.get(i).content.isEmpty()) {
            // No content
            holder.content.setVisibility(View.GONE);
        } else {
            // Yes content
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setText(chapter.posts.get(i).content);
        }
        if (chapter.posts.get(i).photoUrl != null) {
            // Yes photo
            holder.image.setAdjustViewBounds(true);
            Glide.with(context).load(chapter.posts.get(i).photoUrl).into(holder.image);
            holder.image.setMaxHeight(800);
        } else {
            // No photo
            holder.image.setImageDrawable(null);
        }
        // Set date
        holder.date.setText(chapter.posts.get(i).timestamp.toString());

        Meeting meeting = chapter.posts.get(i).meeting;
        if (meeting != null) {
            // Yes meeting
            holder.meeting.setVisibility(View.VISIBLE);
            // Set title to meeting date
            String title = (meeting.month + 1) + "-" + meeting.day + "-" + meeting.year + " Meeting";
            holder.meetingDate.setText(title);

            holder.checkIn.setOnClickListener(v -> {
                // Clicked check in to meeting, trigger event handler
                Button button = (Button) v;
                listener.onCheckIn(button, i);
            });

            // Trigger event handler on click
            holder.viewAttendees.setOnClickListener(v -> listener.onViewMembers(i));

        } else {
            // No meeting
            holder.meeting.setVisibility(View.GONE);
        }

        if (chapter.posts.get(i).author.equals(uid)) {
            // Allow ability to remove post
            holder.removePost.setVisibility(View.VISIBLE);
            // Handled by activity
            holder.removePost.setOnClickListener(v -> listener.onRemove(i));
        } else {
            holder.removePost.setVisibility(View.GONE);
            holder.removePost.setClickable(false);
        }

        return convertView;
    }
}