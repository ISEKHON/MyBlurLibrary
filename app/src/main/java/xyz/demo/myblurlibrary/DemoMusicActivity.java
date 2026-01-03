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

    private NavBarContainer navBarContainer;
    private NavigationBar navigationBar;
    private MiniPlayer miniPlayer;
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
        
        // Add animated gradient views behind navbar for blur visualization
        addBackgroundGradients(rootLayout);
        
        // Create unified navbar container with blur
        createNavBarContainer(rootLayout);
        
        setContentView(rootLayout);
    }
    
    private void addBackgroundGradients(FrameLayout root) {
        // Gradient 1 - Purple to Pink
        View gradient1 = new View(this);
        android.graphics.drawable.GradientDrawable grad1 = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            new int[]{0xFF9C27B0, 0xFFE91E63}
        );
        grad1.setCornerRadius(dp(20));
        gradient1.setBackground(grad1);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(dp(200), dp(150));
        params1.gravity = Gravity.BOTTOM | Gravity.START;
        params1.setMargins(dp(20), 0, 0, dp(20));
        root.addView(gradient1, params1);
        
        // Gradient 2 - Blue to Cyan
        View gradient2 = new View(this);
        android.graphics.drawable.GradientDrawable grad2 = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.BR_TL,
            new int[]{0xFF2196F3, 0xFF00BCD4}
        );
        grad2.setCornerRadius(dp(20));
        gradient2.setBackground(grad2);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(dp(180), dp(120));
        params2.gravity = Gravity.BOTTOM | Gravity.END;
        params2.setMargins(0, 0, dp(20), dp(50));
        root.addView(gradient2, params2);
        
        // Gradient 3 - Orange to Red
        View gradient3 = new View(this);
        android.graphics.drawable.GradientDrawable grad3 = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xFFFF9800, 0xFFF44336}
        );
        grad3.setCornerRadius(dp(20));
        gradient3.setBackground(grad3);
        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(dp(150), dp(100));
        params3.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params3.setMargins(0, 0, 0, dp(10));
        root.addView(gradient3, params3);
        
        // Animate gradients
        gradient1.animate()
            .rotation(360f)
            .setDuration(20000)
            .setInterpolator(new android.view.animation.LinearInterpolator())
            .setListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {}
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    gradient1.setRotation(0);
                    gradient1.animate()
                        .rotation(360f)
                        .setDuration(20000)
                        .setInterpolator(new android.view.animation.LinearInterpolator())
                        .start();
                }
                @Override
                public void onAnimationCancel(android.animation.Animator animation) {}
                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {}
            })
            .start();
        
        gradient2.animate()
            .scaleX(1.2f).scaleY(1.2f)
            .setDuration(3000)
            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                gradient2.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(3000)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
            })
            .start();
        
        gradient3.animate()
            .translationY(-dp(20))
            .setDuration(2000)
            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction(() -> {
                gradient3.animate()
                    .translationY(0)
                    .setDuration(2000)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();
            })
            .start();
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createNavBarContainer(FrameLayout root) {
        // Create container with blur
        navBarContainer = new NavBarContainer(this);
        navBarContainer.setNavBarBackgroundColor(0x80000000);
        
        // Create NavigationBar (without its own blur)
        navigationBar = new NavigationBar(this);
        navigationBar.setBlurEnabled(false); // Container handles blur
        navigationBar.setNavBarBackgroundColor(0x00000000); // Transparent
        navigationBar.setInactiveIconColor(0xFFAAAAAA);
        navigationBar.setActiveIconColor(0xFFFFFFFF);
        navigationBar.setCornerRadius(25f);
        
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
        
        // Create MiniPlayer (without its own blur)
        miniPlayer = new MiniPlayer(this);
        miniPlayer.setBlurEnabled(false); // Container handles blur
        miniPlayer.setBackgroundColor(0x00000000); // Transparent
        miniPlayer.setCornerRadius(25f);
        miniPlayer.setSongInfo("Summer Vibes", "Chill Artists");
        miniPlayer.setAlbumArtColor(0xFF1DB954);
        
        // Set listener
        miniPlayer.setOnMiniPlayerListener(new MiniPlayer.OnMiniPlayerListener() {
            @Override
            public void onPlayPause(boolean isPlaying) {
                Toast.makeText(DemoMusicActivity.this, 
                    isPlaying ? "Playing" : "Paused", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onPrevious() {
                Toast.makeText(DemoMusicActivity.this, "Previous", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onNext() {
                Toast.makeText(DemoMusicActivity.this, "Next", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onMiniPlayerClick() {
                Toast.makeText(DemoMusicActivity.this, "Open Full Player", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Add components to container
        navBarContainer.setMiniPlayer(miniPlayer);
        navBarContainer.setNavigationBar(navigationBar);
        
        // Position container at bottom
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        containerParams.setMargins(dp(16), 0, dp(16), dp(24));
        root.addView(navBarContainer, containerParams);
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
        content.addView(createMusicCard("Summer Vibes", "Playlist • 24 songs", 0xFF1DB954, true));
        content.addView(createMusicCard("Chill Beats", "Playlist • 38 songs", 0xFF2196F3, false));
        
        // Recommended section
        content.addView(createSectionTitle("Recommended for You"));
        content.addView(createMusicCard("Rock Classics", "Playlist • 50 songs", 0xFFE91E63, false));
        content.addView(createMusicCard("Jazz Evening", "Playlist • 42 songs", 0xFFFF9800, false));
        
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
    
    private View createMusicCard(String title, String subtitle, int color, boolean showPlayButton) {
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
        
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.weight = 1;
        card.addView(textContainer, textParams);
        
        // Add play button if needed
        if (showPlayButton) {
            ImageView playButton = new ImageView(this);
            playButton.setImageResource(android.R.drawable.ic_media_play);
            playButton.setColorFilter(Color.WHITE);
            playButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            playButton.setPadding(dp(8), dp(8), dp(8), dp(8));
            
            android.graphics.drawable.GradientDrawable playBg = new android.graphics.drawable.GradientDrawable();
            playBg.setColor(0xFF1DB954);
            playBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            playButton.setBackground(playBg);
            
            LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            playParams.gravity = Gravity.CENTER_VERTICAL;
            card.addView(playButton, playParams);
            
            // Click listener to toggle mini player
            playButton.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    toggleMiniPlayer(title, subtitle, color);
                }
            });
        }
        
        return card;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void toggleMiniPlayer(String songTitle, String artist, int albumColor) {
        if (navBarContainer.isMiniPlayerVisible()) {
            navBarContainer.hideMiniPlayer();
        } else {
            miniPlayer.setSongInfo(songTitle, artist);
            miniPlayer.setAlbumArtColor(albumColor);
            miniPlayer.setPlaying(true);
            navBarContainer.showMiniPlayer();
        }
    }
    
    // Overloaded method for backward compatibility
    private View createMusicCard(String title, String subtitle, int color) {
        return createMusicCard(title, subtitle, color, false);
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
