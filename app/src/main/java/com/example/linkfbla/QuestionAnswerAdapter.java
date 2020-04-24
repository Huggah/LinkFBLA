package com.example.linkfbla;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class QuestionAnswerAdapter extends BaseAdapter {
    private Context context;
    private Chapter chapter;
    private String uid;
    private AdapterEventListener listener;

    // Delegate for lambda allows activity to handle event
    interface AdapterEventListener {
        void onClickRemove(int i, boolean reply);
    }

    // holder views in memory
    static class ViewHolder {
        ImageView questionProfilePicture;
        ImageView questionRemove;
        TextView questionName;
        TextView questionContent;
        ImageView replyProfilePicture;
        ImageView replyRemove;
        TextView replyName;
        TextView replyContent;
        View reply;
    }

    QuestionAnswerAdapter(Context context, Chapter chapter, String uid, AdapterEventListener listener) {
        this.context = context;
        this.chapter = chapter;
        this.uid = uid;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return chapter.questions.size();
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
        // Reverse order, most recent on top
        int i = chapter.questions.size() - position - 1;

        ViewHolder holder = new ViewHolder();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.question_answer, parent, false);
            holder.questionProfilePicture = convertView.findViewById(R.id.question_profile_pic);
            holder.questionRemove = convertView.findViewById(R.id.question_remove);
            holder.questionName = convertView.findViewById(R.id.question_name);
            holder.questionContent = convertView.findViewById(R.id.question_content);
            holder.replyProfilePicture = convertView.findViewById(R.id.reply_profile_pic);
            holder.replyRemove = convertView.findViewById(R.id.reply_remove);
            holder.replyName = convertView.findViewById(R.id.reply_name);
            holder.replyContent = convertView.findViewById(R.id.reply_content);
            holder.reply = convertView.findViewById(R.id.reply);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QuestionAnswer post = chapter.questions.get(i);
        // Update UI for question
        updateUI(holder.questionProfilePicture, holder.questionRemove, holder.questionName, holder.questionContent, i, post, false);
        if (post.reply != null) {
            // Has reply, update UI for reply
            updateUI(holder.replyProfilePicture, holder.replyRemove, holder.replyName, holder.replyContent, i, chapter.questions.get(i).reply, true);
            holder.reply.setVisibility(View.VISIBLE);
        } else {
            holder.reply.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void updateUI(ImageView profilePicture, ImageView remove, TextView name, TextView content, int i, QuestionAnswer post, boolean reply) {
        UserProfile author = chapter.members.get(post.author);
        if (author == null) return;
        // User profile picture
        if (author.photoUrl != null) {
            Glide.with(context).load(author.photoUrl).centerCrop().apply(RequestOptions.circleCropTransform()).into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.ic_linkfbla_icon);
        }
        if (post.author.equals(uid)) {
            // Add option to remove post / reply
            remove.setOnClickListener(v -> listener.onClickRemove(i, reply));
            remove.setVisibility(View.VISIBLE);
        } else {
            remove.setClickable(false);
            remove.setVisibility(View.GONE);
        }
        // Set name of author
        name.setText(author.name);
        // Set content text to content
        content.setText(post.content);
    }
}
