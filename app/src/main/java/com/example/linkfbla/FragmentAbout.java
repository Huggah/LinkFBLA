package com.example.linkfbla;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentAbout extends Fragment {
    private HomeActivity activity;

    // IDs of social media links for FBLA National
    private static final String FACEBOOK_ID = "130215063657029";
    private static final String FACEBOOK_URL = "FutureBusinessLeaders";
    private static final String INSTAGRAM = "fbla_pbl";
    private static final String TWITTER = "FBLA_National";
    private static final String WEBSITE_URL = "https://www.fbla-pbl.org/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        activity = (HomeActivity)getActivity();

        ImageView facebook = view.findViewById(R.id.about_facebook);
        ImageView instagram = view.findViewById(R.id.about_instagram);
        ImageView twitter = view.findViewById(R.id.about_twitter);
        ImageView website = view.findViewById(R.id.about_website);

        // Add Facebook link
        facebook.setOnClickListener(v -> {
            Intent intent;
            try {
                activity.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + FACEBOOK_ID));
            } catch (Exception e) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + FACEBOOK_URL));
            }
            startActivity(intent);
        });

        // Add Instagram link
        instagram.setOnClickListener(v -> {
            Uri uri = Uri.parse("http://instagram.com/_u/" + INSTAGRAM);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + INSTAGRAM)));
            }
        });

        // Add Twitter link
        twitter.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + TWITTER));
            startActivity(intent);
        });

        // Add link to website
        website.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
            startActivity(intent);
        });

        WebView webView = view.findViewById(R.id.about_web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.fbla-pbl.org/about/");

        // Disables any links on website
        webView.setOnTouchListener((v, event) -> true);

        return view;
    }
}
