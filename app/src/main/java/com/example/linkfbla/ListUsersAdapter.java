package com.example.linkfbla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class ListUsersAdapter extends BaseAdapter {

    private ArrayList<UserProfile> profiles;
    private Context context;

    public ListUsersAdapter(Context context, ArrayList<UserProfile> profiles) {
        this.context = context;
        this.profiles = profiles;
    }

    // ViewHolder to keep user info in memory
    static class ViewHolder {
        ImageView profilePicture;
        TextView userName;
        TextView userEmail;
    }

    @Override
    public int getCount() {
        return profiles.size();
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
        int i = getCount() - position - 1;

        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.user, parent, false);
            holder.profilePicture = convertView.findViewById(R.id.user_profile_picture);
            holder.userName = convertView.findViewById(R.id.user_name);
            holder.userEmail = convertView.findViewById(R.id.user_email);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (profiles.get(i).photoUrl != null) {
            Glide.with(context).load(profiles.get(i).photoUrl)
                    .centerCrop().apply(RequestOptions.circleCropTransform()).into(holder.profilePicture);
        } else {
            holder.profilePicture.setImageResource(R.drawable.ic_linkfbla_icon);
        }
        holder.userName.setText(profiles.get(i).name);
        holder.userEmail.setText(profiles.get(i).email);

        return convertView;
    }
}