package com.example.linkfbla;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ViewEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        // Unpack URL intent
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        String url = data.getString("url");

        // Go to URL
        WebView webView = findViewById(R.id.event_page);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // Finished loading, disable loading spinner
                ProgressBar loading = findViewById(R.id.loading_page);
                loading.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(url);
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