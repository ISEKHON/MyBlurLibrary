package xyz.demo.myblurlibrary;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import xyz.code.navigationbar.NavigationBar;

/**
 * Demo Activity showcasing NavigationBar component with all features
 */
public class NavDemoActivity extends Activity {

    private enum ThemeMode {
        AUTO, LIGHT, DARK
    }

    private NavigationBar navigationBar;
    private TextView statusText;
    private LinearLayout demoContent;
    private ThemeMode currentThemeMode = ThemeMode.AUTO;
    private FrameLayout rootLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupUI();
    }
    
    private void setupUI() {
        // Detect theme based on current mode
        boolean isDarkMode = isDarkModeActive();
        
        // Root container
        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(isDarkMode ? Color.parseColor("#121212") : Color.parseColor("#F5F5F5"));
        
        // Scrollable content
        ScrollView scrollView = new ScrollView(this);
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(16), dp(16), dp(16), dp(120)); // Bottom padding for navbar
        
        // Title
        TextView title = new TextView(this);
        title.setText("NavigationBar Component Demo");
        title.setTextSize(24);
        title.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(16));
        contentLayout.addView(title);
        
        // Status text
        statusText = new TextView(this);
        statusText.setText("Active Tab: Home (0)");
        statusText.setTextSize(16);
        statusText.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(dp(16), dp(8), dp(16), dp(16));
        statusText.setBackgroundColor(isDarkMode ? 0x20FFFFFF : 0x20000000);
        contentLayout.addView(statusText);
        
        // Demo content section
        demoContent = new LinearLayout(this);
        demoContent.setOrientation(LinearLayout.VERTICAL);
        demoContent.setPadding(0, dp(16), 0, 0);
        contentLayout.addView(demoContent);
        
        // Add demo buttons
        addDemoButtons(isDarkMode);
        
        // Add gradient views for blur visualization
        addGradientVisualizationSection(isDarkMode);
        
        scrollView.addView(contentLayout);
        rootLayout.addView(scrollView);
        
        // Create NavigationBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            createNavigationBar(rootLayout, isDarkMode);
        } else {
            Toast.makeText(this, "NavigationBar requires Android 12+", Toast.LENGTH_LONG).show();
        }
        
        setContentView(rootLayout);
    }
    
    private boolean isDarkModeActive() {
        switch (currentThemeMode) {
            case LIGHT:
                return false;
            case DARK:
                return true;
            case AUTO:
            default:
                return (getResources().getConfiguration().uiMode & 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                    android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
    }
    
    private void switchThemeMode(ThemeMode mode) {
        currentThemeMode = mode;
        // Clear all views
        rootLayout.removeAllViews();
        // Recreate UI with new theme
        setupUI();
        
        String modeText = mode == ThemeMode.AUTO ? "Auto" : 
                         mode == ThemeMode.LIGHT ? "Light" : "Dark";
        Toast.makeText(this, "Theme: " + modeText, Toast.LENGTH_SHORT).show();
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void createNavigationBar(FrameLayout root, boolean isDarkMode) {
        navigationBar = new NavigationBar(this);
        
        // Configure styling
        navigationBar.setNavBarBackgroundColor(isDarkMode ? 0x80252525 : 0x80E0E0E0);
        navigationBar.setActiveBackgroundColor(0xB0CDDC39); // Yellow-green with transparency
        navigationBar.setInactiveIconColor(isDarkMode ? 0xFFFFFFFF : 0xFF666666);
        navigationBar.setActiveIconColor(0xFF000000);
        navigationBar.setCornerRadius(100f); // Pill shape
        navigationBar.setBlurEnabled(true);
        
        // Add tabs (using same icons as navbar_tabs.xml)
        navigationBar
            .addTab(R.drawable.outline_home_24, "Home")
            .addTab(R.drawable.outline_search_24, "Library")
            .addTab(R.drawable.outline_history_24, "Recent")
            .addTab(R.drawable.outline_schedule_24, "Saved")
            .addTab(R.drawable.outline_settings_24, "Settings");
        
        // Set initial badges
        navigationBar.setBadge(1, 5);  // Library has 5 notifications
        navigationBar.setBadge(2, 99); // Recent has 99+
        
        // Set listener
        navigationBar.setOnTabSelectedListener((position, tab) -> {
            statusText.setText("Active Tab: " + tab.label + " (" + position + ")");
            Toast.makeText(this, "Selected: " + tab.label, Toast.LENGTH_SHORT).show();
        });
        
        // Position at bottom center with dynamic width based on tab count
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        int margin = dp(8);
        params.setMargins(margin, 0, margin, dp(16));
        
        root.addView(navigationBar, params);
    }
    
    private void addDemoButtons(boolean isDarkMode) {
        // Section: Theme Control
        addSectionTitle("Theme Control", isDarkMode);
        
        LinearLayout themeButtonRow = new LinearLayout(this);
        themeButtonRow.setOrientation(LinearLayout.HORIZONTAL);
        themeButtonRow.setGravity(Gravity.CENTER);
        
        Button autoThemeBtn = createThemeButton("Auto", isDarkMode);
        autoThemeBtn.setOnClickListener(v -> switchThemeMode(ThemeMode.AUTO));
        if (currentThemeMode == ThemeMode.AUTO) {
            autoThemeBtn.setBackgroundColor(0xFF4CAF50);
        }
        
        Button lightThemeBtn = createThemeButton("Light", isDarkMode);
        lightThemeBtn.setOnClickListener(v -> switchThemeMode(ThemeMode.LIGHT));
        if (currentThemeMode == ThemeMode.LIGHT) {
            lightThemeBtn.setBackgroundColor(0xFF4CAF50);
        }
        
        Button darkThemeBtn = createThemeButton("Dark", isDarkMode);
        darkThemeBtn.setOnClickListener(v -> switchThemeMode(ThemeMode.DARK));
        if (currentThemeMode == ThemeMode.DARK) {
            darkThemeBtn.setBackgroundColor(0xFF4CAF50);
        }
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        btnParams.setMargins(dp(4), 0, dp(4), 0);
        
        themeButtonRow.addView(autoThemeBtn, btnParams);
        themeButtonRow.addView(lightThemeBtn, btnParams);
        themeButtonRow.addView(darkThemeBtn, btnParams);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dp(16));
        demoContent.addView(themeButtonRow, rowParams);
        
        // Section: Animation Curves
        addSectionTitle("Animation Curves", isDarkMode);
        
        // Row 1: Linear, Accelerate, Decelerate
        LinearLayout animRow1 = new LinearLayout(this);
        animRow1.setOrientation(LinearLayout.HORIZONTAL);
        animRow1.setGravity(Gravity.CENTER);
        
        Button linearBtn = createAnimButton("Linear", isDarkMode);
        linearBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.LINEAR);
            showToast("Animation: Linear");
        });
        
        Button accelBtn = createAnimButton("Accelerate", isDarkMode);
        accelBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.ACCELERATE);
            showToast("Animation: Accelerate");
        });
        
        Button decelBtn = createAnimButton("Decelerate", isDarkMode);
        decelBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.DECELERATE);
            showToast("Animation: Decelerate");
        });
        
        animRow1.addView(linearBtn, btnParams);
        animRow1.addView(accelBtn, btnParams);
        animRow1.addView(decelBtn, btnParams);
        demoContent.addView(animRow1);
        
        // Row 2: AccelDecel, Overshoot, Anticipate
        LinearLayout animRow2 = new LinearLayout(this);
        animRow2.setOrientation(LinearLayout.HORIZONTAL);
        animRow2.setGravity(Gravity.CENTER);
        
        Button accelDecelBtn = createAnimButton("Accel Decel", isDarkMode);
        accelDecelBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.ACCELERATE_DECELERATE);
            showToast("Animation: Accelerate Decelerate");
        });
        
        Button overshootBtn = createAnimButton("Overshoot", isDarkMode);
        overshootBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.OVERSHOOT);
            showToast("Animation: Overshoot (Default)");
        });
        
        Button anticipateBtn = createAnimButton("Anticipate", isDarkMode);
        anticipateBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.ANTICIPATE);
            showToast("Animation: Anticipate");
        });
        
        animRow2.addView(accelDecelBtn, btnParams);
        animRow2.addView(overshootBtn, btnParams);
        animRow2.addView(anticipateBtn, btnParams);
        demoContent.addView(animRow2);
        
        // Row 3: Anticipate Overshoot, Bounce, Elastic
        LinearLayout animRow3 = new LinearLayout(this);
        animRow3.setOrientation(LinearLayout.HORIZONTAL);
        animRow3.setGravity(Gravity.CENTER);
        
        Button anticipateOverBtn = createAnimButton("Antic Over", isDarkMode);
        anticipateOverBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.ANTICIPATE_OVERSHOOT);
            showToast("Animation: Anticipate Overshoot");
        });
        
        Button bounceBtn = createAnimButton("Bounce", isDarkMode);
        bounceBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.BOUNCE);
            showToast("Animation: Bounce");
        });
        
        Button elasticBtn = createAnimButton("Elastic", isDarkMode);
        elasticBtn.setOnClickListener(v -> {
            navigationBar.setAnimationCurve(NavigationBar.AnimationCurve.ELASTIC);
            showToast("Animation: Elastic");
        });
        
        animRow3.addView(anticipateOverBtn, btnParams);
        animRow3.addView(bounceBtn, btnParams);
        animRow3.addView(elasticBtn, btnParams);
        demoContent.addView(animRow3);
        
        // Animation Duration Control
        LinearLayout durationRow = new LinearLayout(this);
        durationRow.setOrientation(LinearLayout.HORIZONTAL);
        durationRow.setGravity(Gravity.CENTER);
        
        Button fastBtn = createAnimButton("Fast (200ms)", isDarkMode);
        fastBtn.setOnClickListener(v -> {
            navigationBar.setAnimationDuration(200);
            showToast("Duration: 200ms");
        });
        
        Button normalBtn = createAnimButton("Normal (400ms)", isDarkMode);
        normalBtn.setOnClickListener(v -> {
            navigationBar.setAnimationDuration(400);
            showToast("Duration: 400ms");
        });
        
        Button slowBtn = createAnimButton("Slow (700ms)", isDarkMode);
        slowBtn.setOnClickListener(v -> {
            navigationBar.setAnimationDuration(700);
            showToast("Duration: 700ms");
        });
        
        durationRow.addView(fastBtn, btnParams);
        durationRow.addView(normalBtn, btnParams);
        durationRow.addView(slowBtn, btnParams);
        demoContent.addView(durationRow);
        
        // Section: Tab Management
        addSectionTitle("Tab Management", isDarkMode);
        
        addButton("Get Tab Count", v -> {
            int count = navigationBar.getTabCount();
            showToast("Tab Count: " + count);
        }, isDarkMode);
        
        addButton("Add New Tab", v -> {
            int count = navigationBar.getTabCount();
            try {
                navigationBar.addTab(android.R.drawable.ic_menu_add, "Tab " + (count + 1));
                showToast("Added Tab " + (count + 1));
            } catch (IllegalStateException e) {
                showToast("Cannot add more tabs. Maximum is " + navigationBar.getMaxTabs());
            }
        }, isDarkMode);
        
        addButton("Remove Last Tab", v -> {
            int count = navigationBar.getTabCount();
            if (count > 0) {
                navigationBar.removeTab(count - 1);
                showToast("Removed last tab");
            }
        }, isDarkMode);
        
        addButton("Clear All Tabs", v -> {
            navigationBar.clearTabs();
            showToast("All tabs cleared");
        }, isDarkMode);
        
        // Section: Tab Selection
        addSectionTitle("Tab Selection", isDarkMode);
        
        addButton("Select Tab 0", v -> {
            navigationBar.setActiveTab(0);
        }, isDarkMode);
        
        addButton("Select Tab 2", v -> {
            navigationBar.setActiveTab(2);
        }, isDarkMode);
        
        addButton("Get Active Tab", v -> {
            NavigationBar.NavigationTab tab = navigationBar.getActiveTab();
            if (tab != null) {
                showToast("Active: " + tab.label + " at index " + navigationBar.getActiveTabIndex());
            }
        }, isDarkMode);
        
        addButton("Animate Tab 1", v -> {
            navigationBar.animateTab(1);
        }, isDarkMode);
        
        // Section: Badges
        addSectionTitle("Badge Management", isDarkMode);
        
        addButton("Set Badge on Tab 0 (10)", v -> {
            navigationBar.setBadge(0, 10);
            showToast("Badge set to 10");
        }, isDarkMode);
        
        addButton("Increment Badge on Tab 1", v -> {
            int current = navigationBar.getBadge(1);
            navigationBar.setBadge(1, current + 1);
            showToast("Badge: " + (current + 1));
        }, isDarkMode);
        
        addButton("Clear Badge on Tab 1", v -> {
            navigationBar.clearBadge(1);
            showToast("Badge cleared");
        }, isDarkMode);
        
        addButton("Get Badge Count (Tab 2)", v -> {
            int count = navigationBar.getBadge(2);
            showToast("Tab 2 Badge: " + count);
        }, isDarkMode);
        
        // Section: Enable/Disable
        addSectionTitle("Enable/Disable Tabs", isDarkMode);
        
        addButton("Disable Tab 3", v -> {
            navigationBar.setTabEnabled(3, false);
            showToast("Tab 3 disabled");
        }, isDarkMode);
        
        addButton("Enable Tab 3", v -> {
            navigationBar.setTabEnabled(3, true);
            showToast("Tab 3 enabled");
        }, isDarkMode);
        
        addButton("Check Tab 3 Status", v -> {
            boolean enabled = navigationBar.isTabEnabled(3);
            showToast("Tab 3 is " + (enabled ? "enabled" : "disabled"));
        }, isDarkMode);
        
        // Section: Tag Management
        addSectionTitle("Tag Management", isDarkMode);
        
        addButton("Set Tag on Tab 0", v -> {
            navigationBar.setTabTag(0, "home_screen");
            showToast("Tag set: home_screen");
        }, isDarkMode);
        
        addButton("Get Tag from Tab 0", v -> {
            Object tag = navigationBar.getTabTag(0);
            showToast("Tag: " + (tag != null ? tag.toString() : "null"));
        }, isDarkMode);
        
        addButton("Find Tab by Tag", v -> {
            int index = navigationBar.findTabByTag("home_screen");
            showToast("Tab with tag 'home_screen': " + index);
        }, isDarkMode);
        
        // Section: Styling
        addSectionTitle("Styling Options", isDarkMode);
        
        addButton("Toggle Blur Effect", v -> {
            boolean enabled = !navigationBar.isBlurEnabled();
            navigationBar.setBlurEnabled(enabled);
            showToast("Blur " + (enabled ? "enabled" : "disabled"));
        }, isDarkMode);
        
        addButton("Change Active Color (Blue)", v -> {
            navigationBar.setActiveBackgroundColor(0xFF2196F3);
            showToast("Active color changed to blue");
        }, isDarkMode);
        
        addButton("Change Active Color (Green)", v -> {
            navigationBar.setActiveBackgroundColor(0xFF4CAF50);
            showToast("Active color changed to green");
        }, isDarkMode);
        
        addButton("Reset Active Color", v -> {
            navigationBar.setActiveBackgroundColor(0xFFCDDC39);
            showToast("Color reset to yellow-green");
        }, isDarkMode);
        
        addButton("Change Badge Color (Purple)", v -> {
            navigationBar.setBadgeColors(0xFF9C27B0, 0xFFFFFFFF);
            showToast("Badge color changed");
        }, isDarkMode);
        
        addButton("Change Corner Radius (20dp)", v -> {
            navigationBar.setCornerRadius(20f);
            showToast("Corner radius: 20dp");
        }, isDarkMode);
        
        addButton("Change Corner Radius (Pill)", v -> {
            navigationBar.setCornerRadius(100f);
            showToast("Corner radius: Pill shape");
        }, isDarkMode);
        
        // Section: Advanced
        addSectionTitle("Advanced Operations", isDarkMode);
        
        addButton("Get All Tab Info", v -> {
            StringBuilder info = new StringBuilder("Tabs:\n");
            for (int i = 0; i < navigationBar.getTabCount(); i++) {
                NavigationBar.NavigationTab tab = navigationBar.getTab(i);
                if (tab != null) {
                    info.append(i).append(": ").append(tab.label)
                        .append(" (Badge: ").append(tab.badgeCount)
                        .append(", Enabled: ").append(tab.enabled)
                        .append(")\n");
                }
            }
            showToast(info.toString());
        }, isDarkMode);
        
        addButton("Reset Demo", v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recreate();
            }
        }, isDarkMode);
    }
    
    private void addGradientVisualizationSection(boolean isDarkMode) {
        addSectionTitle("Blur Visualization", isDarkMode);
        
        TextView description = new TextView(this);
        description.setText("Gradients below help visualize the blur effect on the navigation bar");
        description.setTextSize(14);
        description.setTextColor(isDarkMode ? 0xAAFFFFFF : 0xAA000000);
        description.setGravity(Gravity.CENTER);
        description.setPadding(dp(16), 0, dp(16), dp(12));
        demoContent.addView(description);
        
        // Create gradient container
        LinearLayout gradientContainer = new LinearLayout(this);
        gradientContainer.setOrientation(LinearLayout.VERTICAL);
        gradientContainer.setPadding(0, 0, 0, dp(16));
        
        // Static gradients
        gradientContainer.addView(createGradientView(
            new int[]{0xFFE91E63, 0xFF9C27B0},
            "Pink to Purple"
        ));
        
        gradientContainer.addView(createGradientView(
            new int[]{0xFF2196F3, 0xFF00BCD4},
            "Blue to Cyan"
        ));
        
        gradientContainer.addView(createGradientView(
            new int[]{0xFF4CAF50, 0xFF8BC34A},
            "Green to Light Green"
        ));
        
        gradientContainer.addView(createGradientView(
            new int[]{0xFFFF9800, 0xFFFF5722},
            "Orange to Deep Orange"
        ));
        
        // Animated gradient
        View animatedGradient = createAnimatedGradientView();
        gradientContainer.addView(animatedGradient);
        
        demoContent.addView(gradientContainer);
    }
    
    private View createGradientView(int[] colors, String label) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, dp(12));
        
        // Gradient view
        View gradientView = new View(this);
        android.graphics.drawable.GradientDrawable gradient = 
            new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                colors
            );
        float cornerRadius = getResources().getDimension(R.dimen.gradient_corner_radius);
        gradient.setCornerRadius(cornerRadius);
        gradientView.setBackground(gradient);
        
        int gradientHeight = getResources().getDimensionPixelSize(R.dimen.gradient_height);
        int gradientMarginBottom = getResources().getDimensionPixelSize(R.dimen.gradient_margin_bottom);
        LinearLayout.LayoutParams gradientParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            gradientHeight
        );
        gradientParams.setMargins(0, 0, 0, gradientMarginBottom);
        container.addView(gradientView, gradientParams);
        
        // Label
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
            getResources().getDimension(R.dimen.text_size_small));
        labelView.setTextColor(isDarkModeActive() ? 0x80FFFFFF : 0x80000000);
        labelView.setGravity(Gravity.CENTER);
        container.addView(labelView);
        
        return container;
    }
    
    private View createAnimatedGradientView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, dp(12));
        
        // Animated gradient view
        View gradientView = new View(this) {
            private float hue = 0f;
            private Runnable animator = new Runnable() {
                @Override
                public void run() {
                    hue += 2f;
                    if (hue >= 360f) hue = 0f;
                    
                    int color1 = Color.HSVToColor(new float[]{hue, 0.8f, 0.9f});
                    int color2 = Color.HSVToColor(new float[]{(hue + 60) % 360, 0.8f, 0.9f});
                    int color3 = Color.HSVToColor(new float[]{(hue + 120) % 360, 0.8f, 0.9f});
                    
                    android.graphics.drawable.GradientDrawable gradient = 
                        new android.graphics.drawable.GradientDrawable(
                            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                            new int[]{color1, color2, color3}
                        );
                    gradient.setCornerRadius(getContext().getResources().getDimension(R.dimen.gradient_corner_radius));
                    setBackground(gradient);
                    
                    postDelayed(this, 50);
                }
            };
            
            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                post(animator);
            }
            
            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                removeCallbacks(animator);
            }
        };
        
        int gradientHeight = getResources().getDimensionPixelSize(R.dimen.gradient_height);
        int gradientMarginBottom = getResources().getDimensionPixelSize(R.dimen.gradient_margin_bottom);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            gradientHeight
        );
        params.setMargins(0, 0, 0, gradientMarginBottom);
        container.addView(gradientView, params);
        
        // Label
        TextView labelView = new TextView(this);
        labelView.setText("Animated Rainbow Gradient");
        labelView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
            getResources().getDimension(R.dimen.text_size_small));
        labelView.setTextColor(0x80FFFFFF);
        labelView.setGravity(Gravity.CENTER);
        container.addView(labelView);
        
        return container;
    }
    
    private Button createThemeButton(String text, boolean isDarkMode) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(isDarkMode ? 0x40FFFFFF : 0x40000000);
        button.setPadding(dp(12), dp(10), dp(12), dp(10));
        return button;
    }
    
    private Button createAnimButton(String text, boolean isDarkMode) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(10);
        button.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        button.setBackgroundColor(isDarkMode ? 0x30CDDC39 : 0x304CAF50);
        button.setPadding(dp(4), dp(8), dp(4), dp(8));
        return button;
    }
    
    private void addSectionTitle(String title, boolean isDarkMode) {
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
            getResources().getDimension(R.dimen.text_size_section_title));
        sectionTitle.setTextColor(isDarkMode ? 0xFFCDDC39 : 0xFF4CAF50);
        int sectionPaddingTop = getResources().getDimensionPixelSize(R.dimen.demo_section_padding_top);
        int sectionPaddingBottom = getResources().getDimensionPixelSize(R.dimen.demo_section_padding_bottom);
        sectionTitle.setPadding(0, sectionPaddingTop, 0, sectionPaddingBottom);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        demoContent.addView(sectionTitle, params);
    }
    
    private void addButton(String text, View.OnClickListener listener, boolean isDarkMode) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
        button.setBackgroundColor(isDarkMode ? 0x30FFFFFF : 0x30000000);
        int paddingH = getResources().getDimensionPixelSize(R.dimen.demo_button_padding_horizontal);
        int paddingV = getResources().getDimensionPixelSize(R.dimen.demo_button_padding_vertical);
        button.setPadding(paddingH, paddingV, paddingH, paddingV);
        button.setOnClickListener(listener);
        
        int buttonMarginBottom = getResources().getDimensionPixelSize(R.dimen.demo_button_margin_bottom);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, buttonMarginBottom);
        demoContent.addView(button, params);
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
