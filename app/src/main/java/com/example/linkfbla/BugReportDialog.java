package com.example.linkfbla;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BugReportDialog extends AppCompatDialogFragment {

    private TextView bugName;
    private TextView bugDescription;

    private AppCompatActivity activity;
    // Listener
    private OnCancelListener listener;

    // Delegate for lambda function so that activity can handle it
    interface OnCancelListener {
        void onCancel();
    }

    public BugReportDialog(OnCancelListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        activity = (AppCompatActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AppTheme));

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.bug_report_dialog, null);
        bugName = view.findViewById(R.id.bug_title);
        bugDescription = view.findViewById(R.id.bug_description);

        builder.setView(view)
                .setTitle("Report a Bug")
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .setPositiveButton("Ok", sendBugReportListener);

        return builder.create();
    }

    private DialogInterface.OnClickListener sendBugReportListener = (dialog, which) -> {
        if (bugDescription.getText().toString().isEmpty() || bugName.getText().toString().isEmpty()) return;

        String messageText;

        try {
            HomeActivity homeActivity = (HomeActivity) activity;

            // User is logged in, yes profile data
            UserProfile user = homeActivity.chapter.members.get(homeActivity.user.getUid());
            if (user == null) return;

            messageText = user.name + "\n" +
                    homeActivity.chapter_name + "\n" +
                    user.email + "\n\n" +
                    "Description:\n" + bugDescription.getText().toString();
        } catch (ClassCastException e) {
            // User isn't logged in, no profile data
            messageText = "Anonymous:\n" + bugDescription.getText().toString();
        }

        String subjectText = "BUG REPORT! " + bugName.getText().toString();

        // Send email from LinkFBLA service account to me.
        JavaMailAPI javaMailAPI = new JavaMailAPI(activity, subjectText, messageText, successful -> {
            if (successful) {
                Toast.makeText(activity, "Bug report sent.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Bug report failed to send.", Toast.LENGTH_SHORT).show();
            }
        });
        javaMailAPI.execute();
    };

    // Execute callback function
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        listener.onCancel();
    }

    // This method is overridden to prevent saved state from different activity from causing fatal
    // error. The state of the dialog does not need to be saved.
    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
