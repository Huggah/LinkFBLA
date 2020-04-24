package com.example.linkfbla;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        email = findViewById(R.id.etResetEmail);
        findViewById(R.id.btnResetPassword).setOnClickListener(v -> {
            // Reset password
            String email_text = email.getText().toString().trim();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (!email_text.isEmpty()) {
                // Send reset email to email address provided
                firebaseAuth.sendPasswordResetEmail(email_text).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PasswordResetActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Email address invalid
                        Toast.makeText(PasswordResetActivity.this, "Could not send password reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(PasswordResetActivity.this, "Enter your registered email.", Toast.LENGTH_SHORT).show();
            }
        });
        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Back button logic
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}