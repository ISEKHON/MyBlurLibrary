package xyz.demo.myblurlibrary;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

/**
 * Demo Music Activity showcasing NavigationBar in a music app context
 */
public class DemoMusicActivity extends Activity {

    private NavigationBar navigationBar;
    private FrameLayout contentFrame;
    private FrameLayout rootLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setupUI();
        } else {
            Toast.makeText(this, "This demo requires Android 12+", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void setupUI() {
        // Root container with dark background
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#1a1a1a"));
        
        // Content frame
        contentFrame = new FrameLayout(this);
        contentFrame.setPadding(0, 0, 0, dp(100)); // Bottom padding for navbar
        rootLayout.addView(contentFrame, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));
        
        // Show home content initially
        showHomeContent();
        
        // Create NavigationBar
        createNavigationBar(rootLayout);
        
        setContentView(rootLayout);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createNavigationBar(FrameLayout root) {
        navigationBar = new NavigationBar(this);
        
        // Configure styling - dark theme with glass effect
        navigationBar.setNavBarBackgroundColor(0x80000000); // Semi-transparent black
        navigationBar.setInactiveIconColor(0xFFAAAAAA); // Gray
        navigationBar.setActiveIconColor(0xFFFFFFFF); // White
        navigationBar.setCornerRadius(100f); // Pill shape
        navigationBar.setBlurEnabled(true);
        
        // Add music app tabs
        navigationBar
            .addTab(R.drawable.outline_home_24, "Home")
            .addTab(R.drawable.outline_search_24, "Search")
            .addTab(R.drawable.outline_history_24, "Library")
            .addTab(R.drawable.outline_schedule_24, "Playlists")
            .addTab(R.drawable.outline_settings_24, "Settings");
        
        // Set listener
        navigationBar.setOnTabSelectedListener((position, tab) -> {
            switch (position) {
                case 0:
                    showHomeContent();
                    break;
                case 1:
                    showSearchContent();
                    break;
                case 2:
                    showLibraryContent();
                    break;
                case 3:
                    showPlaylistsContent();
                    break;
                case 4:
                    showSettingsContent();
                    break;
            }
        });
        
        // Position navbar at bottom
        FrameLayout.LayoutParams navbarParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        navbarParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        navbarParams.setMargins(dp(16), 0, dp(16), dp(24));
        root.addView(navigationBar, navbarParams);
    }
    
    private void showHomeContent() {
        contentFrame.removeAllViews();
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        
        // Title
        TextView title = createTitle("Music Home");
        content.addView(title);
        
        // Recently Played section
        content.addView(createSectionTitle("Recently Played"));
        content.addView(createMusicCard("Summer Vibes", "Playlist • 24 songs", 0xFF1DB954));
        content.addView(createMusicCard("Chill Beats", "Playlist • 38 songs", 0xFF2196F3));
        
        // Recommended section
        content.addView(createSectionTitle("Recommended for You"));
        content.addView(createMusicCard("Rock Classics", "Playlist • 50 songs", 0xFFE91E63));
        content.addView(createMusicCard("Jazz Evening", "Playlist • 42 songs", 0xFFFF9800));
        
        scrollView.addView(content);
        contentFrame.addView(scrollView);
    }
    
    private void showSearchContent() {
        contentFrame.removeAllViews();
        
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(20), dp(100), dp(20), dp(20));
        
        TextView title = createTitle("Search Music");
        content.addView(title);
        
        TextView subtitle = new TextView(this);
        subtitle.setText("Find your favorite songs, albums, and artists");
        subtitle.setTextSize(16);
        subtitle.setTextColor(0xFFAAAAAA);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(20));
        content.addView(subtitle);
        
        // Search categories
        content.addView(createSearchCategory("🎵", "Songs"));
        content.addView(createSearchCategory("💿", "Albums"));
        content.addView(createSearchCategory("🎤", "Artists"));
        content.addView(createSearchCategory("🎧", "Podcasts"));
        
        contentFrame.addView(content);
    }
    
    private void showLibraryContent() {
        contentFrame.removeAllViews();
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        
        TextView title = createTitle("Your Library");
        content.addView(title);
        
        content.addView(createLibraryItem("Liked Songs", "124 songs", 0xFF1DB954));
        content.addView(createLibraryItem("Downloaded", "89 songs", 0xFF2196F3));
        content.addView(createLibraryItem("Albums", "12 albums", 0xFFE91E63));
        content.addView(createLibraryItem("Artists", "45 artists", 0xFFFF9800));
        content.addView(createLibraryItem("Podcasts", "8 shows", 0xFF9C27B0));
        
        scrollView.addView(content);
        contentFrame.addView(scrollView);
    }
    
    private void showPlaylistsContent() {
        contentFrame.removeAllViews();
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        
        TextView title = createTitle("Playlists");
        content.addView(title);
        
        content.addView(createMusicCard("Workout Mix", "15 songs • 1h 2m", 0xFFFF5722));
        content.addView(createMusicCard("Study Focus", "32 songs • 2h 15m", 0xFF673AB7));
        content.addView(createMusicCard("Road Trip", "28 songs • 1h 48m", 0xFF00BCD4));
        content.addView(createMusicCard("Party Hits", "45 songs • 2h 55m", 0xFFFFEB3B));
        
        scrollView.addView(content);
        contentFrame.addView(scrollView);
    }
    
    private void showSettingsContent() {
        contentFrame.removeAllViews();
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));
        
        TextView title = createTitle("Settings");
        content.addView(title);
        
        content.addView(createSettingsItem("Account", "Manage your profile"));
        content.addView(createSettingsItem("Playback", "Audio quality and offline"));
        content.addView(createSettingsItem("Notifications", "What to be notified about"));
        content.addView(createSettingsItem("Privacy", "Control your data"));
        content.addView(createSettingsItem("About", "Version and legal info"));
        
        scrollView.addView(content);
        contentFrame.addView(scrollView);
    }
    
    private TextView createTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setPadding(0, 0, 0, dp(20));
        return title;
    }
    
    private TextView createSectionTitle(String text) {
        TextView section = new TextView(this);
        section.setText(text);
        section.setTextSize(20);
        section.setTextColor(Color.WHITE);
        section.setPadding(0, dp(16), 0, dp(12));
        return section;
    }
    
    private View createMusicCard(String title, String subtitle, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0x30FFFFFF);
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);
        
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);
        
        // Colored square
        View colorBox = new View(this);
        android.graphics.drawable.GradientDrawable colorBg = new android.graphics.drawable.GradientDrawable();
        colorBg.setColor(color);
        colorBg.setCornerRadius(dp(8));
        colorBox.setBackground(colorBg);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(dp(60), dp(60));
        boxParams.setMargins(0, 0, dp(16), 0);
        card.addView(colorBox, boxParams);
        
        // Text container
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        titleText.setTextColor(Color.WHITE);
        textContainer.addView(titleText);
        
        TextView subtitleText = new TextView(this);
        subtitleText.setText(subtitle);
        subtitleText.setTextSize(14);
        subtitleText.setTextColor(0xFFAAAAAA);
        textContainer.addView(subtitleText);
        
        card.addView(textContainer);
        
        return card;
    }
    
    private View createSearchCategory(String emoji, String text) {
        LinearLayout category = new LinearLayout(this);
        category.setOrientation(LinearLayout.HORIZONTAL);
        category.setGravity(Gravity.CENTER_VERTICAL);
        category.setPadding(dp(20), dp(16), dp(20), dp(16));
        
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0x30FFFFFF);
        bg.setCornerRadius(dp(12));
        category.setBackground(bg);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        category.setLayoutParams(params);
        
        TextView emojiText = new TextView(this);
        emojiText.setText(emoji);
        emojiText.setTextSize(32);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emojiParams.setMargins(0, 0, dp(16), 0);
        category.addView(emojiText, emojiParams);
        
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(18);
        textView.setTextColor(Color.WHITE);
        category.addView(textView);
        
        return category;
    }
    
    private View createLibraryItem(String title, String subtitle, int color) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(16), dp(16), dp(16), dp(16));
        
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0x30FFFFFF);
        bg.setCornerRadius(dp(12));
        item.setBackground(bg);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        item.setLayoutParams(params);
        
        // Colored circle
        View circle = new View(this);
        android.graphics.drawable.GradientDrawable circleBg = new android.graphics.drawable.GradientDrawable();
        circleBg.setColor(color);
        circleBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circle.setBackground(circleBg);
        LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        circleParams.setMargins(0, 0, dp(16), 0);
        item.addView(circle, circleParams);
        
        // Text container
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        titleText.setTextColor(Color.WHITE);
        textContainer.addView(titleText);
        
        TextView subtitleText = new TextView(this);
        subtitleText.setText(subtitle);
        subtitleText.setTextSize(14);
        subtitleText.setTextColor(0xFFAAAAAA);
        textContainer.addView(subtitleText);
        
        item.addView(textContainer);
        
        return item;
    }
    
    private View createSettingsItem(String title, String subtitle) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(16), dp(16), dp(16), dp(16));
        
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0x30FFFFFF);
        bg.setCornerRadius(dp(12));
        item.setBackground(bg);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        item.setLayoutParams(params);
        
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        titleText.setTextColor(Color.WHITE);
        item.addView(titleText);
        
        TextView subtitleText = new TextView(this);
        subtitleText.setText(subtitle);
        subtitleText.setTextSize(14);
        subtitleText.setTextColor(0xFFAAAAAA);
        subtitleText.setPadding(0, dp(4), 0, 0);
        item.addView(subtitleText);
        
        return item;
    }
    
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
