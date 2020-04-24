package com.example.linkfbla;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

// Used as an "are you sure?" statement i.e. for removing data
public class ConfirmDialog extends AppCompatDialogFragment {

    // Callback Listener
    private ConfirmListener listener;
    private String message;
    private String title;

    public ConfirmDialog(String title, String message, ConfirmListener listener) {
        this.listener = listener;
        this.message = message;
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setMessage(message)
        .setNegativeButton("cancel", (dialog, which) -> { })
        .setPositiveButton("ok", (dialog, which) -> listener.execute());
        return builder.create();
    }

    // Delegate for lambda so activity can handle when dialog is confirmed.
    interface ConfirmListener {
        void execute();
    }
}
