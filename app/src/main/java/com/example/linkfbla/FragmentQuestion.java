package com.example.linkfbla;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentQuestion extends Fragment {

    private static final String TAG = "FragmentQuestion";

    private HomeActivity activity;
    private ListView questionsListView;
    private TextView noPostsText;
    private EditText content;
    private ImageView post;
    private View replyTag;
    private boolean canPost;
    private boolean reply;
    private int replyTo;

    // Lambda called by adapter, when X is clicked at position i
    private QuestionAnswerAdapter.AdapterEventListener removeListener = (i, reply) -> {
        ConfirmDialog dialog = new ConfirmDialog("Remove Post", "Are you sure you want to remove this post?", () -> {
            // Clicked OK on dialog box
            String path;
            if (reply) {
                // Remove reply, set reply at index i to null, no need to rearrange post indices
                path = Chapter.DATABASE_PATH + activity.chapter_name + "/questions/" + i + "/reply";
                DatabaseReference ref = activity.firebaseDatabase.getReference(path);
                ref.setValue(null).addOnSuccessListener(aVoid -> {
                    refreshPage();
                }).addOnFailureListener(e -> Log.w(TAG, e));
            } else {
                // Remove entire post, need to remove from list then overwrite entire list.
                // TODO: Make removing list a request to server, so won't have to send so much data
                ArrayList<QuestionAnswer> posts = activity.chapter.questions;
                posts.remove(i);
                path = Chapter.DATABASE_PATH + activity.chapter_name + "/questions/";
                DatabaseReference ref = activity.firebaseDatabase.getReference(path);
                ref.setValue(posts).addOnSuccessListener(aVoid -> {
                    refreshPage();
                }).addOnFailureListener(e -> Log.w(TAG, e));
            }
        });
        dialog.show(activity.getSupportFragmentManager(), "remove post dialog");
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        activity = (HomeActivity) getActivity();

        replyTag = view.findViewById(R.id.reply_to);
        post = view.findViewById(R.id.post);
        content = view.findViewById(R.id.content);
        noPostsText = view.findViewById(R.id.no_posts_yet);
        questionsListView = view.findViewById(R.id.questions_list);

        refreshPage();

        questionsListView.setOnItemClickListener((parent, view1, position, id) -> {
            int i = questionsListView.getCount() - position - 1;
            if (activity.chapter.adminPermissions.containsKey(activity.user.getUid()) && activity.chapter.questions.get(i).reply == null) {
                // User is admin and no existing reply, can reply
                reply = true;
                replyTo = i;
                replyTag.setVisibility(View.VISIBLE);
                TextView replyText = replyTag.findViewById(R.id.reply_to_text);
                replyText.setText("Reply to: " + activity.chapter.members.get(activity.chapter.questions.get(i).author).name);
            }
        });

        // Cancel reply
        replyTag.setOnClickListener(v -> {
            reply = false;
            replyTag.setVisibility(View.GONE);
        });

        // Trigger updating add event icon enabled state after typing post.
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // canPost is true when there is text in editText
                canPost = !content.getText().toString().isEmpty();
                updatePostIcon();
            }
        });

        post.setOnClickListener(v -> {
            if (canPost) {
                String path;
                if (reply) {
                    // Path to reply reference in post at index replyTo
                    path = Chapter.DATABASE_PATH + activity.chapter_name + "/questions/" + replyTo + "/reply";
                } else {
                    // Add to end of list in database
                    path = Chapter.DATABASE_PATH + activity.chapter_name + "/questions/" + activity.chapter.questions.size();
                }
                post(path);
            }
        });

        // Refresh icon w/ animation
        ImageView refresh = view.findViewById(R.id.refresh);
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.spin);
        refresh.setOnClickListener(v -> {
            refresh.startAnimation(anim);
            refreshPage();
        });

        return view;
    }

    private void refreshPage() {
        if (activity.chapter.questions.size() > 0) {
            // Events, configure listView
            noPostsText.setVisibility(View.GONE);
            questionsListView.setVisibility(View.VISIBLE);
            QuestionAnswerAdapter adapter = new QuestionAnswerAdapter(activity, activity.chapter, activity.user.getUid(), removeListener);
            questionsListView.setAdapter(adapter);
        } else {
            noPostsText.setVisibility(View.VISIBLE);
            questionsListView.setVisibility(View.GONE);
        }
    }

    private void updatePostIcon() {
        // Post icon color corresponds with whether or not you can post
        if (canPost) {
            post.setImageResource(R.drawable.ic_send_enabled);
        } else {
            post.setImageResource(R.drawable.ic_send);
        }
    }

    private void post(String path) {
        DatabaseReference ref = activity.firebaseDatabase.getReference(path);
        ref.setValue(new QuestionAnswer(activity.user.getUid(), content.getText().toString())).addOnSuccessListener(aVoid -> {
            content.setText("");
            updatePostIcon();
            refreshPage();
            reply = false;
            replyTag.setVisibility(View.GONE);
        }).addOnFailureListener(e -> Log.w(TAG, e));
    }
}
