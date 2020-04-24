package com.example.linkfbla;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {

    private HomeActivity activity;
    private Uri imagePath;
    private ImageView imagePreview, post, close, meetingIcon;
    private EditText content;
    private ListView posts;
    private boolean hasContent;
    private boolean hasImage;
    private boolean hasMeeting;
    private Meeting meeting;
    private View view;

    private static final int UPLOAD_PHOTO = 420;
    private static final String TAG = "FragmentHome";

    // Callbacks for when meeting buttons are pressed. Triggered by OnClickListeners for buttons
    // in posts ListView.
    private PostAdapter.AdapterEventListener listener = new PostAdapter.AdapterEventListener() {
        @Override
        public void onCheckIn(final Button button, int i) {
            final DatabaseReference ref = activity.firebaseDatabase
                    .getReference(Chapter.DATABASE_PATH + activity.chapter_name + "/posts/" + i + "/meeting/attendees");
            ArrayList<String> attendees = new ArrayList<>();
            try {
                attendees = activity.chapter.posts.get(i).meeting.attendees;
            } catch (NullPointerException exception) {
                // Some error in loading ListView in that its item's meeting box is enabled, so
                // has invalid position where meeting object of post is null in database.
                Log.w(TAG, exception);
            }
            boolean checkedIn = attendees != null && attendees.contains(activity.user.getUid());
            // Add if not already, else remove from list.
            if (!checkedIn) {
                int count = attendees.size();
                ref.child(Integer.toString(count)).setValue(activity.user.getUid()).addOnFailureListener(e -> {
                    Log.w(TAG, e);
                }).addOnSuccessListener(aVoid -> Toast.makeText(activity, "Checked in.", Toast.LENGTH_SHORT).show());
            } else {
                attendees.remove(activity.user.getUid());
                ref.setValue(attendees).addOnSuccessListener(aVoid -> Toast.makeText(activity, "Revoked checkin.", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onViewMembers(int i) {
            Meeting meeting = activity.chapter.posts.get(i).meeting;
            if (meeting != null) {
                Intent intent = new Intent(activity, ListUsersActivity.class);
                intent.putExtra("Uids", meeting.attendees);
                startActivity(intent);
            }
        }

        @Override
        public void onRemove(final int i) {
            // TODO: Change client-side list removal to sending removal request to server

            ConfirmDialog dialog = new ConfirmDialog("Remove Post", "Are you sure you want to remove this post?", () -> {
                ArrayList<ChapterPost> posts = activity.chapter.posts;
                posts.remove(i);
                DatabaseReference ref = activity.firebaseDatabase.getReference(Chapter.DATABASE_PATH + activity.chapter_name + "/posts");
                ref.setValue(posts).addOnSuccessListener(aVoid -> refreshPosts()).addOnFailureListener(e -> {
                    Toast.makeText(activity, "Couldn't remove post.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, e);
                });
            });
            dialog.show(activity.getSupportFragmentManager(), "remove post dialog");
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (HomeActivity) getActivity();

        // Get views
        view = inflater.inflate(R.layout.fragment_home, container, false);
        content = (EditText) view.findViewById(R.id.content);
        ImageView upload_photo = (ImageView) view.findViewById(R.id.upload_photo);
        meetingIcon = (ImageView) view.findViewById(R.id.meeting);
        post = (ImageView) view.findViewById(R.id.post);
        close = (ImageView) view.findViewById(R.id.close_button);
        imagePreview = (ImageView) view.findViewById(R.id.image_preview);
        posts = (ListView) view.findViewById(R.id.post_list);

        ImageView refresh = view.findViewById(R.id.refresh);
        Animation anim = AnimationUtils.loadAnimation(activity, R.anim.spin);
        refresh.setOnClickListener(v -> {
            refresh.startAnimation(anim);
            refreshPosts();
        });
        refreshPosts();

        post.setOnClickListener(v -> {
            // Check if post has some sort of content
            if (!(hasImage || hasContent || hasMeeting))
                return;

            final DatabaseReference ref = activity.firebaseDatabase.getReference(Chapter.DATABASE_PATH + activity.chapter_name + "/posts");
            Toast.makeText(activity, "Sending post...", Toast.LENGTH_LONG).show();
            try {
                final int length = activity.chapter.posts.size();
                if (imagePath != null) {
                    // Sending an image is more complex, so separate block of code. Send to storage,
                    // fetch image URL, and send that to database along with the rest of the post data
                    StorageReference storageRef = activity.firebaseStorage.getReference("Chapters/" + activity.chapter_name + "/Images/post" + length);
                    UploadTask uploadTask = storageRef.putFile(imagePath);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            ChapterPost post = new ChapterPost(activity.user.getUid(), content.getText().toString(), uri.toString(), new Date());
                            post.meeting = meeting;
                            send_post(post, ref.child(Integer.toString(length)));
                        }).addOnFailureListener(e -> Toast.makeText(activity, "Failed to post.", Toast.LENGTH_SHORT).show());
                    }).addOnFailureListener(e -> Toast.makeText(activity, "Failed to post.", Toast.LENGTH_SHORT).show());;
                } else {
                    ChapterPost post = new ChapterPost(activity.user.getUid(), content.getText().toString(), null, new Date());
                    post.meeting = meeting;
                    send_post(post, ref.child(Integer.toString(length)));
                }
            } catch (NullPointerException exception) {
                Log.w(TAG, exception);
            }
        });

        // Gets rid of post image
        close.setOnClickListener(v -> {
            close.setVisibility(View.GONE);
            imagePath = null;
            imagePreview.setImageDrawable(null);
            hasImage = false;
            updatePostIcon();
        });

        // First click creates meeting, second click removes it
        meetingIcon.setOnClickListener(v -> {
            if (meeting == null) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(activity, (view1, year, month, dayOfMonth) -> {
                    meeting = new Meeting(dayOfMonth, month, year);
                    meetingIcon.setImageResource(R.drawable.ic_date_enabled);
                    hasMeeting = true;
                    updatePostIcon();
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            } else {
                meeting = null;
                meetingIcon.setImageResource(R.drawable.ic_date);
                hasMeeting = false;
                updatePostIcon();
            }
        });

        upload_photo.setOnClickListener(v -> {
            // Intent to open gallery for image
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), UPLOAD_PHOTO);
        });

        // Trigger updating send button enabled state after typing post.
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                hasContent = !content.getText().toString().isEmpty();
                updatePostIcon();
            }
        });

        return view;
    }

    private void refreshPosts() {
        PostAdapter adapter = new PostAdapter(activity, activity.chapter, activity.user.getUid(), listener);
        posts.setAdapter(adapter);
        if (activity.chapter.adminPermissions.containsKey(activity.user.getUid())) {
            View postHeader = view.findViewById(R.id.home_header);
            postHeader.setVisibility(View.VISIBLE);
        }
    }

    private void send_post(ChapterPost post, DatabaseReference ref) {
        if (meeting != null)
            FragmentCalendar.addEvent(activity, (meeting.month + 1) + "-" + meeting.day + "-" + meeting.year, "Club Meeting", aVoid1 -> {});
        ref.setValue(post).addOnSuccessListener(aVoid -> {
            // Get rid of any content in user input
            close.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            hasImage = false;
            content.setText("");
            meeting = null;
            hasMeeting = false;
            meetingIcon.setImageResource(R.drawable.ic_date);
            updatePostIcon();
            refreshPosts();
        }).addOnFailureListener(e -> Toast.makeText(activity, "Failed to post.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == UPLOAD_PHOTO && resultCode == RESULT_OK) {
            imagePath = data.getData();
            imagePreview.setImageURI(imagePath);
            imagePreview.setAdjustViewBounds(true);
            int length = content.getWidth();
            // Image preview fits screen proportions
            imagePreview.setMaxHeight(length / 2);
            imagePreview.setMaxWidth(length);
            hasImage = true;
            close.setVisibility(View.VISIBLE);
            updatePostIcon();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Called when there is a change in post content. Matches enabled/disabled post icon with
    // send conditions.
    private void updatePostIcon() {
        if (hasContent || hasImage || hasMeeting) {
            post.setImageResource(R.drawable.ic_send_enabled);
        } else {
            post.setImageResource(R.drawable.ic_send);
        }
    }
}
