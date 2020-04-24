package com.example.linkfbla;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private TextView profilePrompt;
    private EditText userName, userEmail, password, confirmPassword;
    private FirebaseAuth firebaseAuth;
    private ImageView profile_picture;
    private Uri imagePath;
    private FirebaseStorage firebaseStorage;

    private static int PICK_IMAGE = 69;
    private static String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        profilePrompt = findViewById(R.id.register_profile_pic_prompt);
        userName = findViewById(R.id.etUserName);
        userEmail = findViewById(R.id.etUserEmail);
        password = findViewById(R.id.etPassword);
        confirmPassword = findViewById(R.id.etConfirmPassword);
        profile_picture = findViewById(R.id.register_profile_picture);
        Button joinButton = findViewById(R.id.register_btnRegister);
        Button googleButton = findViewById(R.id.register_btnGoogleRegister);
        Button facebookButton = findViewById(R.id.register_btnFacebookRegister);
        MainActivity mainActivity = MainActivity.getInstance();
        final Button googleLogin = mainActivity.findViewById(R.id.main_btnGoogleLogIn);
        final Button facebookLogin = mainActivity.findViewById(R.id.main_btnFacebookLoginBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        joinButton.setOnClickListener(v -> {
            if (validate()) {
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                if (!prefs.getBoolean("agreeToTerms", false)) {
                    TermsOfUseDialog dialog = new TermsOfUseDialog(agreed -> {
                        if (agreed) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("agreeToTerms", true);
                            editor.apply();
                            createUser();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "terms of use dialog");
                } else {
                    createUser();
                }
            }
        });

        // Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Simulates a click on login buttons in MainActivity because there is no need to register
        // if using a provider.
        googleButton.setOnClickListener(v -> {
            googleLogin.performClick();
            finish();
        });
        facebookButton.setOnClickListener(v -> {
            facebookLogin.performClick();
            finish();
        });

        profile_picture.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
        });

        // Add links to terms of use and privacy policy
        // Click to view our terms of use and privacy policy
        TextView policyText = findViewById(R.id.terms_and_privacy_text);
        SpannableString ss = new SpannableString(policyText.getText().toString());
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(RegisterActivity.this, ViewEventActivity.class);
                intent.putExtra("url", "file:///android_asset/terms_and_conditions.html");
                startActivity(intent);
            }
        }, 18, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(RegisterActivity.this, ViewEventActivity.class);
                intent.putExtra("url", "file:///android_asset/privacy_policy.html");
                startActivity(intent);
            }
        }, 35, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        policyText.setText(ss);
        policyText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void createUser() {
        // Upload data to the database
        String user_email = userEmail.getText().toString().trim();
        String user_password = password.getText().toString().trim();

        firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                confirm();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handles one activity result: profile image selected from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            Glide.with(getApplicationContext()).load(imagePath.toString())
                    .centerCrop().apply(RequestOptions.circleCropTransform()).into(profile_picture);
            profilePrompt.setVisibility(View.INVISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Validates if register form is filled correctly
    private boolean validate() {
        String userEmail = this.userEmail.getText().toString();
        String password = this.password.getText().toString();
        String confirmPassword = this.confirmPassword.getText().toString();

        if (password.isEmpty() || confirmPassword.isEmpty() || userEmail.isEmpty()) {
            Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }

        return false;
    }

    // Called on registration, sequence of tasks necessary for registration.
    // Sends email verification, uploads profile image to storage, adds image url to profile
    private void confirm() {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Email verification successful
                if (imagePath != null) {
                    final StorageReference imageRef = firebaseStorage.getReference("Users/" + user.getUid() + "/profile");
                    UploadTask uploadTask = imageRef.putFile(imagePath);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        // Image upload to storage successful
                        // Needs permission to read from storage, so added on failure listener
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Get Download URL successful
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
                            user.updateProfile(request).addOnFailureListener(exception -> {
                                Log.w(TAG, "updateDownloadUrl:failure", exception);
                                // No need for authentication anymore, sign out
                                firebaseAuth.signOut();
                            }).addOnSuccessListener(aVoid -> {
                                // Success! Registration process went accordingly.
                                firebaseAuth.signOut();
                            });
                        }).addOnFailureListener(exception -> {
                            Log.w(TAG, "getDownloadUrl:failure", exception);
                            // No need for authentication anymore, sign out
                            firebaseAuth.signOut();
                        });
                    }).addOnFailureListener(exception -> {
                        Log.w(TAG, "uploadImageToStorage:failure", exception);
                        // No need for authentication anymore, sign out
                        firebaseAuth.signOut();
                    });
                } else {
                    firebaseAuth.signOut();
                }

                // After verification email has sent, profile picture not crucial to a
                // successful registration, so we can submit the user's name asynchronously, and
                // finish registration process once profile update is complete.
                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(userName.getText().toString()).build();
                user.updateProfile(request).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Successfully Registered! Check your email for verification.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Could not initialize user profile.", Toast.LENGTH_SHORT).show();
                        user.delete();
                    }
                });

            } else {
                Toast.makeText(RegisterActivity.this, "Verification mail not sent.", Toast.LENGTH_SHORT).show();
                user.delete();
            }
        });
    }

    // Back button logic
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
