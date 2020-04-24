package com.example.linkfbla;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class FragmentAddOfficers extends Fragment {

    // position index to name
    private static final String[] POSITION_NAMES = {"President", "Vice President", "Secretary", "Treasurer", "Public Relations Officer"};
    private static final String TAG = "FragmentAddOfficers";

    private HomeActivity activity;
    private View[] positionViews = new View[5];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_officers, container, false);
        activity = ((HomeActivity) getActivity());

        // Index in view array corresponds to position
        positionViews[0] = view.findViewById(R.id.president);
        positionViews[1] = view.findViewById(R.id.vicePresident);
        positionViews[2] = view.findViewById(R.id.secretary);
        positionViews[3] = view.findViewById(R.id.treasurer);
        positionViews[4] = view.findViewById(R.id.publicRelations);

        refreshPage();

        return view;
    }

    private void refreshPage() {
        String permission = activity.chapter.adminPermissions.get(activity.user.getUid());
        boolean isAdvisor = permission != null && permission.equals("advisor");
        for (int i = 0; i < POSITION_NAMES.length; i++) {

            TextView name = positionViews[i].findViewById(R.id.user_name);
            TextView email = positionViews[i].findViewById(R.id.user_email);
            ImageView profilePicture = positionViews[i].findViewById(R.id.user_profile_picture);

            if (activity.chapter.positions.containsKey(POSITION_NAMES[i])) {
                // There is an officer at position i, update graphics
                String uid = activity.chapter.positions.get(POSITION_NAMES[i]);

                UserProfile profile = activity.chapter.members.get(uid);
                if (profile == null) continue;
                name.setText(profile.name);
                email.setText(profile.email);

                if (profile.photoUrl != null) {
                    Glide.with(activity).load(profile.photoUrl)
                            .centerCrop().apply(RequestOptions.circleCropTransform()).into(profilePicture);
                } else {
                    profilePicture.setImageResource(R.drawable.ic_linkfbla_icon);
                }

                if (isAdvisor) {
                    final int I = i;
                    positionViews[i].setOnClickListener(v -> {
                        // Clicked on officer as advisor, remove officer
                        ConfirmDialog dialog = new ConfirmDialog("Remove Officer", "Are you sure you want to remove this officer?", () -> {
                            // Save member uid because it will be deleted from positions in database, and
                            // we will need it later because it is a reference in adminPermissions
                            String member = activity.chapter.positions.get(POSITION_NAMES[I]);
                            String positionPath = Chapter.DATABASE_PATH + activity.chapter_name + "/positions/" + POSITION_NAMES[I];
                            DatabaseReference positionRef = activity.firebaseDatabase.getReference(positionPath);
                            // Get rid of value in HashMap
                            positionRef.setValue(null).addOnSuccessListener(aVoid -> {
                                // Used saved member uid for reference to adminPermissions to remove
                                // member from there as well
                                String permissionPath = Chapter.DATABASE_PATH + activity.chapter_name + "/adminPermissions/" + member;
                                DatabaseReference permissionRef = activity.firebaseDatabase.getReference(permissionPath);
                                permissionRef.setValue(null).addOnSuccessListener(aVoid1 -> refreshPage()).addOnFailureListener(e -> Log.w(TAG, e));
                            }).addOnFailureListener(e -> Log.w(TAG, e));
                        });
                        dialog.show(activity.getSupportFragmentManager(), "remove officer dialog");
                    });
                }
            } else if (isAdvisor) {
                // No officer, but advisor, add click listener to add officer. Update graphics too.
                final int I = i;
                positionViews[i].setOnClickListener(v -> {
                    Intent intent = new Intent(activity, ListUsersActivity.class);

                    // List of all user uids who are not advisors
                    ArrayList<String> uids = new ArrayList<>();
                    String[] membersList = activity.chapter.members.keySet().toArray(new String[0]);
                    for (String member : membersList) {
                        if (activity.chapter.adminPermissions.get(member) != null)
                            if (activity.chapter.adminPermissions.get(member).equals("advisor"))
                                continue;

                        uids.add(member);
                    }
                    intent.putExtra("Uids", uids);
                    startActivityForResult(intent, I);
                });

                name.setText("Add Officer");
                email.setText("Click here to add a new officer");
                profilePicture.setImageResource(R.drawable.ic_add_circle);
            } else {
                // Not advisor and there is no officer at position i
                positionViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode >= 0 && requestCode <= 4 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            String uid = extras.getString("uid");

            // Don't add officer if they already have an officer position
            if (activity.chapter.positions != null)
                if (activity.chapter.positions.values().contains(uid)) {
                    Toast.makeText(activity, "Member already has an officer position.", Toast.LENGTH_SHORT).show();
                    return;
                }

            // Add officer to adminPermissions and positions in database
            DatabaseReference chapterRef = activity.firebaseDatabase.getReference(Chapter.DATABASE_PATH + activity.chapter_name);
            chapterRef.child("adminPermissions").child(uid).setValue("officer").addOnSuccessListener(task -> {
                chapterRef.child("positions").child(POSITION_NAMES[requestCode]).setValue(uid).addOnSuccessListener(task1 -> {
                    refreshPage();
                }).addOnFailureListener(e -> Log.w(TAG, e));
            }).addOnFailureListener(e -> Log.w(TAG, e));
        }
     }
}
