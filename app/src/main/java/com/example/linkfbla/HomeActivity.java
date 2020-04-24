package com.example.linkfbla;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    public FirebaseAuth firebaseAuth;
    public FirebaseDatabase firebaseDatabase;
    public FirebaseStorage firebaseStorage;
    public FirebaseUser user;

    /**
     * The chapter the current user is a member of. Synchronized to database. To set values in
     * chapter, update the database.
     */
    public Chapter chapter;
    public String chapter_name;

    private boolean setupFragments = true;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private boolean committedTransaction = true;

    private static final String TAG = "HomeActivity";
    private static HomeActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        user = firebaseAuth.getCurrentUser();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(new BugReportListener(this), sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);

        getChapter();
    }

    // Sets up homepage and layout. Called on initialization if user provides a reference to their
    // chapter, or once they join a chapter.
    private void homepage(final String chapter_name) {
        setContentView(R.layout.activity_home);
        this.chapter_name = chapter_name;

        DatabaseReference ref = firebaseDatabase.getReference(Chapter.DATABASE_PATH + chapter_name);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chapter chapter = dataSnapshot.getValue(Chapter.class);
                if (chapter != null) {
                    // Retrieved valid data from database, update chapter on client side.
                    HomeActivity.this.chapter = chapter;

                    // Only set up fragments when first read chapter data, then don't update
                    // fragments when chapter in database is changed.
                    if (setupFragments) {
                        setupFragments = false;

                        // Find header views
                        navigationView = findViewById(R.id.navigation_view);
                        View header = navigationView.getHeaderView(0);
                        TextView header_name = header.findViewById(R.id.nav_header_name);
                        TextView header_position = header.findViewById(R.id.nav_header_position);
                        TextView header_chapter = header.findViewById(R.id.nav_header_chapter);
                        TextView header_identifier = header.findViewById(R.id.nav_header_email);
                        ImageView header_profile_picture = header.findViewById(R.id.header_profile_picture);

                        if (chapter.adminPermissions.containsKey(user.getUid())) {
                            // User is an officer
                            Menu menu = navigationView.getMenu();
                            menu.setGroupVisible(R.id.nav_officer_tools, true);
                            header_position.setText("Administrator");
                            header_position.setVisibility(View.VISIBLE);
                            if (chapter.adminPermissions.get(user.getUid()).equals("advisor")) {
                                // User is an advisor
                                menu.setGroupVisible(R.id.member_pages, false);
                            }

                        }

                        // Setup navigation toolbar and three-line menu
                        toolbar = findViewById(R.id.toolbar);
                        setSupportActionBar(toolbar);

                        drawerLayout = findViewById(R.id.drawer);

                        // Setup header
                        if (user.getPhotoUrl() != null){
                            Glide.with(getApplicationContext()).load(user.getPhotoUrl().toString())
                                    .centerCrop().apply(RequestOptions.circleCropTransform()).into(header_profile_picture);
                        } else {
                            header_profile_picture.setImageResource(R.drawable.ic_linkfbla_icon);
                        }
                        header_name.setText(user.getDisplayName());
                        header_chapter.setText(chapter_name);
                        header_identifier.setText(user.getEmail());

                        // Setup action bar
                        actionBarDrawerToggle = new ActionBarDrawerToggle(HomeActivity.this, drawerLayout, toolbar, R.string.open, R.string.close);
                        drawerLayout.addDrawerListener(actionBarDrawerToggle);
                        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
                        actionBarDrawerToggle.syncState();

                        // load default fragment
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.fragment_container, new FragmentHome());
                        fragmentTransaction.commit();

                        // Check if any menu options are clicked, then open corresponding fragment
                        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

                        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                            @Override
                            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

                            @Override
                            public void onDrawerOpened(@NonNull View drawerView) { }

                            @Override
                            public void onDrawerClosed(@NonNull View drawerView) {
                                if (committedTransaction) return;
                                fragmentTransaction.commit();
                                committedTransaction = true;
                            }

                            @Override
                            public void onDrawerStateChanged(int newState) { }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() != R.id.nav_logout){
                drawerLayout.closeDrawer(GravityCompat.START);
                committedTransaction = false;
            }
            switch (item.getItemId()) {
                case R.id.nav_home:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentHome());
                    break;
                case R.id.nav_about:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentAbout());
                    break;
                case R.id.nav_calendar:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentCalendar());
                    break;
                case R.id.nav_question:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentQuestion());
                    break;
                case R.id.nav_events:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentEvents());
                    break;
                case R.id.nav_officers:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentAddOfficers());
                    break;
                case R.id.nav_events_overview:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentEventsOverview());
                    break;
                case R.id.nav_contact_us:
                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, new FragmentContact());
                    break;
                case R.id.nav_logout:
                    ConfirmDialog dialog = new ConfirmDialog("Log Out","Are you sure you want to log out?", () -> logout());
                    dialog.show(getSupportFragmentManager(), "logout dialog");
                    break;
            }
            return true;
        }
    };

    // Finds chapter the user is in, prompts new users to join.
    private void getChapter() {
        final DatabaseReference ref = firebaseDatabase.getReference("User References/" + user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chapter = dataSnapshot.getValue(String.class);
                if (chapter == null) {
                    joinChapter();
                } else {
                    homepage(chapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    // Called when user reference in database does not mention a chapter.
    private void joinChapter() {
        final String user_name = user.getDisplayName();
        final String user_email = user.getEmail();

        // setup layout and views
        setContentView(R.layout.join_chapter);
        // Include first name in message
        String text = "Hello " + user_name.split(" ")[0] + ". Join a chapter to get started.";
        TextView title = (TextView) findViewById(R.id.join_chapter_title);
        Button join_button = (Button) findViewById(R.id.join_chapter_join_button);
        final EditText chapter_name_text = (EditText) findViewById(R.id.join_chapter_name);
        title.setText(text);

        findViewById(R.id.logoutButton).setOnClickListener(v -> logout());

        join_button.setOnClickListener(v -> {
            chapter_name = chapter_name_text.getText().toString();
            if (chapter_name.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Enter a chapter name.", Toast.LENGTH_LONG).show();
            } else {
                // Form submitted correctly

                String user_url = null;
                try {
                    user_url = user.getPhotoUrl().toString();
                } catch (NullPointerException exception) {
                    Log.e(TAG, "No Profile Picture Url");
                }
                UserProfile profile = new UserProfile(user_name, user_email, user_url);

                DatabaseReference chapterRef = firebaseDatabase.getReference(Chapter.DATABASE_PATH + chapter_name);
                chapterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Chapter chapter = dataSnapshot.getValue(Chapter.class);
                        if (chapter == null) {
                            // Chapter doesn't exist, prompt user to create chapter and join as advisor
                            ConfirmDialog dialog = new ConfirmDialog("New Chapter","This chapter does not exist, you will create a new chapter and join as an advisor.", () -> {
                                DatabaseReference user_ref = firebaseDatabase.getReference("User References/" + user.getUid());
                                user_ref.setValue(chapter_name);

                                // DatabaseReference path includes key (user uid) and adds key value pair to
                                // HashMap members in Chapter class with members and adminPermissions fields
                                DatabaseReference adminRef = firebaseDatabase.getReference(Chapter.DATABASE_PATH + chapter_name + "/adminPermissions/" + user.getUid());
                                DatabaseReference profile_ref = firebaseDatabase.getReference(Chapter.DATABASE_PATH + chapter_name + "/members/" + user.getUid());
                                adminRef.setValue("advisor").addOnCompleteListener(task -> {
                                    if (task.isSuccessful())
                                        profile_ref.setValue(profile).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful())
                                                homepage(chapter_name);
                                        });
                                });
                            });
                            dialog.show(getSupportFragmentManager(), "new chapter dialog");
                        } else {
                            // Chapter already exists, add user to chapter
                            DatabaseReference user_ref = firebaseDatabase.getReference("User References/" + user.getUid());
                            user_ref.setValue(chapter_name);

                            // DatabaseReference path includes key (user uid) and adds key value pair to
                            // HashMap members in Chapter class
                            DatabaseReference profile_ref = firebaseDatabase.getReference(Chapter.DATABASE_PATH + chapter_name + "/members/" + user.getUid());
                            profile_ref.setValue(profile).addOnCompleteListener(task -> {
                                if (task.isSuccessful())
                                    homepage(chapter_name);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        });
    }

    public void logout() {
        firebaseAuth.signOut();
        finish();
        LoginManager.getInstance().logOut();
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
    }

    // Disables back button, otherwise back button will exit the app.
    @Override
    public void onBackPressed() { }

    public static HomeActivity getInstance() {
        return instance;
    }
}
