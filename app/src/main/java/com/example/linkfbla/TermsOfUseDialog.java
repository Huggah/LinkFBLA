package com.example.linkfbla;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class TermsOfUseDialog extends AppCompatDialogFragment {
    private static final String PROMPT = "I agree to the terms of use and privacy policy";

    interface OnAgreeListener {
        void onAgree(boolean agreed);
    }

    private OnAgreeListener listener;

    public TermsOfUseDialog(OnAgreeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AppTheme));

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.terms_of_use_dialog, null);

        CheckBox checkBox = view.findViewById(R.id.checkBox);
        TextView prompt = view.findViewById(R.id.prompt);

        // Spannable String adds links to textView
        SpannableString ss = new SpannableString(PROMPT);

        ClickableSpan termsOfUse = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Go to terms of use
                Intent intent = new Intent(activity, ViewEventActivity.class);
                intent.putExtra("url", "file:///android_asset/terms_and_conditions.html");
                startActivity(intent);
            }
        };

        ClickableSpan privacyPolicy = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Go to privacy policy
                Intent intent = new Intent(activity, ViewEventActivity.class);
                intent.putExtra("url", "file:///android_asset/privacy_policy.html");
                startActivity(intent);
            }
        };

        // Set text
        ss.setSpan(termsOfUse, 15, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacyPolicy, 32, 46, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        prompt.setText(ss);
        prompt.setMovementMethod(LinkMovementMethod.getInstance());

        // Build dialog
        builder.setView(view)
                .setTitle("Terms of use and Privacy Policy")
                .setPositiveButton("Ok", (dialog, which) -> listener.onAgree(checkBox.isChecked()));

        return builder.create();
    }
}
