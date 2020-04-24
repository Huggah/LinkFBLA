package com.example.linkfbla;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText userEmail, password;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseDatabase firebaseDatabase;

    private static final List<String> PERMISSIONS = Arrays.asList("email");
    private static final String WEB_CLIENT_ID = "108249428841-vnecan0mg7g51f2iacg209ks11g45oj3.apps.googleusercontent.com";
    private static final String TAG = "MainActivity";
    private static final int GOOGLE_SIGN_IN = 42069;

    // TODO: Convert register calling methods using MainActivity instance to startActivityForResult
    public static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        setContentView(R.layout.activity_main);
        instance = this;

        userEmail = findViewById(R.id.main_etLoginUserEmail);
        password = findViewById(R.id.main_etLoginPassword);

        TextView register = findViewById(R.id.main_tvNewUser);
        Button signIn = findViewById(R.id.main_btnLogIn);
        Button googleSignInButton = findViewById(R.id.main_btnGoogleLogIn);
        Button facebookSignInButton = findViewById(R.id.main_btnFacebookLoginBtn);
        final LoginButton facebookSignIn = findViewById(R.id.main_btnFacebookLogin);
        TextView forgotPassword = findViewById(R.id.main_tvForgotPassword);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        callbackManager = CallbackManager.Factory.create();
        facebookSignIn.setPermissions(PERMISSIONS);

        facebookSignIn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                Log.d(TAG, "handleFacebookAccessToken:" + token);

                AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
                signInWithCredential(credential);
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Sign-in cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(MainActivity.this, "Error signing in using Facebook", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "signInWithCredential:failure", exception);
            }
        });

        facebookSignInButton.setOnClickListener(v -> {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            if (!prefs.getBoolean("agreeToTerms", false)) {
                TermsOfUseDialog dialog = new TermsOfUseDialog(agreed -> {
                    if (agreed) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("agreeToTerms", true);
                        editor.apply();
                        facebookSignIn.performClick();
                    }
                });
                dialog.show(getSupportFragmentManager(), "terms of use dialog");
            } else {
                facebookSignIn.performClick();
            }
        });

        // **************************
        // UI Event Listeners

        register.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PasswordResetActivity.class)));

        signIn.setOnClickListener(v -> {
            if (userEmail.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter username and password", Toast.LENGTH_SHORT).show();
            } else {
                validateEmail(userEmail.getText().toString(), password.getText().toString());
            }
        });

        googleSignInButton.setOnClickListener(v -> {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            if (!prefs.getBoolean("agreeToTerms", false)) {
                TermsOfUseDialog dialog = new TermsOfUseDialog(agreed -> {
                    if (agreed) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("agreeToTerms", true);
                        editor.apply();
                        googleLogIn();
                    }
                });
                dialog.show(getSupportFragmentManager(), "terms of use dialog");
            } else {
                googleLogIn();
            }
        });

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(new BugReportListener(this), sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
    }

    // Uses credentials from Facebook or Google authentication to sign in.
    // Registers new users to the realtime database.
    private void signInWithCredential(AuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithCredential:success");
                updateActivity();
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(MainActivity.this, "Authentication Failed. " + task.getException(), Toast.LENGTH_LONG).show();
                LoginManager.getInstance().logOut();
            }
        });
    }

    // **************************
    // Google Sign-in Methods
    // **************************

    private void googleLogIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN); // handleSignInResult called when finished
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            signInWithCredential(credential);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // **************************
    // Email Sign-in Methods
    // **************************

    private void validateEmail(String userEmail, String password) {
        firebaseAuth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                checkEmailVerification();
            } else {
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEmailVerification() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert user != null;
        boolean verified = user.isEmailVerified();

        if (verified) {
            updateActivity();
        } else {
            Toast.makeText(this, "Verify your email", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }

    // Handles result from Facebook and Google sign-ins
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data); // Facebook Callback
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // To be called after successful login. Starts new activity.
    private void updateActivity() {
        finish();
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
    }

    // Disables back button, otherwise back button will exit the app.
    @Override
    public void onBackPressed() { }

    public static MainActivity getInstance() {
        return instance;
    }
}
