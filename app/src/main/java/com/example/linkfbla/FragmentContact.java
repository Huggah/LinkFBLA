package com.example.linkfbla;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentContact extends Fragment {
    private HomeActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        activity = (HomeActivity) getActivity();

        // Setup views
        EditText message = view.findViewById(R.id.contact_message);
        EditText subject = view.findViewById(R.id.contact_subject);
        Button send = view.findViewById(R.id.contact_send);

        send.setOnClickListener(v -> {
            if (message.getText().toString().isEmpty() || subject.getText().toString().isEmpty()) {
                Toast.makeText(activity, "Add a message and subject", Toast.LENGTH_SHORT).show();
                return;
            }
            // Form filled correctly
            UserProfile user = activity.chapter.members.get(activity.user.getUid());
            if (user == null) return; // Just in case

            String messageText = user.name + "\n" +
                    activity.chapter_name + "\n" +
                    user.email + "\n\n" +
                    "Message:\n" + message.getText().toString();

            String subjectText = subject.getText().toString();

            // Send email via JavaMail from LinkFBLA service account to me. Includes member's
            // contact info.
            JavaMailAPI javaMailAPI = new JavaMailAPI(activity, subjectText, messageText, successful -> {
                if (successful) {
                    Toast.makeText(activity, "Message sent.", Toast.LENGTH_SHORT).show();
                    message.setText("");
                    subject.setText("");
                } else {
                    Toast.makeText(activity, "Message failed to send.", Toast.LENGTH_SHORT).show();
                }
            });
            javaMailAPI.execute();

        });

        return view;
    }
}
