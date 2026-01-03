package xyz.demo.myblurlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import xyz.code.blur3.DownscaleScrollableNoiseSuppressor;

/**
 * Custom NavigationBar with blur effect, inspired by modern iOS navigation
 * Supports tab management, badges, active states, and custom styling
 */
public class NavigationBar extends FrameLayout {
    
    private static final int MAX_TABS = 5;
    
    private LinearLayout tabContainer;
    private List<NavigationTab> tabs = new ArrayList<>();
    private int activeTabIndex = 0;
    private OnTabSelectedListener listener;
    private boolean blurEnabled = true;
    
    // Blur components
    private DownscaleScrollableNoiseSuppressor noiseSuppressor;
    private android.graphics.Paint paintStrokeTop;
    private android.graphics.Paint paintStrokeBottom;
    private float[] radii = new float[8];
    
    // Styling
    private int backgroundColor = 0x4D1E1E1E; // Dark semi-transparent
    private int activeBackgroundColor = 0xB0CDDC39; // Yellow-green with 70% opacity
    private int inactiveIconColor = 0xFFFFFFFF; // White
    private int activeIconColor = 0xFF000000; // Black
    private float cornerRadius = 100f; // Pill shape
    private int badgeBackgroundColor = 0xFFFF0000; // Red
    private int badgeTextColor = 0xFFFFFFFF; // White
    
    public interface OnTabSelectedListener {
        void onTabSelected(int position, NavigationTab tab);
    }
    
    public static class NavigationTab {
        public int iconRes;
        public String label;
        public int badgeCount = 0;
        public boolean enabled = true;
        public Object tag;
        
        public NavigationTab(int iconRes, String label) {
            this.iconRes = iconRes;
            this.label = label;
        }
    }
    
    public NavigationBar(Context context) {
        super(context);
        init(context);
    }
    
    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        // Initialize blur components
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            noiseSuppressor = new DownscaleScrollableNoiseSuppressor();
            
            paintStrokeTop = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            paintStrokeTop.setStyle(android.graphics.Paint.Style.STROKE);
            paintStrokeTop.setStrokeWidth(dpf(1));
            paintStrokeTop.setColor(0x40FFFFFF);
            
            paintStrokeBottom = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            paintStrokeBottom.setStyle(android.graphics.Paint.Style.STROKE);
            paintStrokeBottom.setStrokeWidth(dpf(0.67f));
            paintStrokeBottom.setColor(0x30FFFFFF);
            
            java.util.Arrays.fill(radii, dp(cornerRadius));
        }
        
        // Create tab container
        tabContainer = new LinearLayout(context);
        tabContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabContainer.setGravity(Gravity.CENTER);
        int paddingH = context.getResources().getDimensionPixelSize(R.dimen.navbar_padding_horizontal);
        int paddingV = context.getResources().getDimensionPixelSize(R.dimen.navbar_padding_vertical);
        tabContainer.setPadding(paddingH, paddingV, paddingH, paddingV);
        
        // Add tab container to this FrameLayout
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.CENTER;
        addView(tabContainer, containerParams);
        
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurEnabled && noiseSuppressor != null) {
            drawBlurBackground(canvas);
        } else {
            // Fallback for older versions or when blur is disabled
            android.graphics.Path path = new android.graphics.Path();
            android.graphics.RectF rect = new android.graphics.RectF(0, 0, getWidth(), getHeight());
            path.addRoundRect(rect, dp(cornerRadius), dp(cornerRadius), android.graphics.Path.Direction.CW);
            canvas.save();
            canvas.clipPath(path);
            canvas.drawColor(backgroundColor);
            canvas.restore();
        }
        
        super.dispatchDraw(canvas);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void drawBlurBackground(Canvas canvas) {
        if (!canvas.isHardwareAccelerated()) {
            return;
        }
        
        // Setup render nodes if needed
        if (noiseSuppressor.getRenderNodesCount() == 0) {
            java.util.ArrayList<android.graphics.RectF> positions = new java.util.ArrayList<>();
            positions.add(new android.graphics.RectF(0, 0, getWidth(), getHeight()));
            noiseSuppressor.setupRenderNodes(positions, 1);
        }
        
        // Record content to blur
        try {
            android.graphics.RecordingCanvas recordingCanvas = noiseSuppressor.beginRecordingRect(0);
            recordingCanvas.save();
            recordingCanvas.translate(-getLeft(), -getTop());
            
            View parent = (View) getParent();
            if (parent != null) {
                int oldVisibility = getVisibility();
                setVisibility(View.INVISIBLE);
                parent.draw(recordingCanvas);
                setVisibility(oldVisibility);
            }
            
            recordingCanvas.restore();
            noiseSuppressor.endRecordingRect();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // Clip to rounded rect
        android.graphics.Path clipPath = new android.graphics.Path();
        android.graphics.RectF clipRect = new android.graphics.RectF(0, 0, getWidth(), getHeight());
        clipPath.addRoundRect(clipRect, radii, android.graphics.Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);
        
        // Draw blurred background
        noiseSuppressor.invalidateResultRenderNodes(getWidth(), getHeight());
        noiseSuppressor.draw(canvas, DownscaleScrollableNoiseSuppressor.DRAW_FROSTED_GLASS);
        
        // Draw overlay
        canvas.drawColor(backgroundColor);
        
        // Draw strokes
        xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
            canvas, 0, 0, getWidth(), getHeight(), radii,
            dpf(1), true, paintStrokeTop
        );
        
        xyz.code.blur3.drawable.BlurredBackgroundDrawable.drawStroke(
            canvas, 0, 0, getWidth(), getHeight(), radii,
            dpf(0.67f), false, paintStrokeBottom
        );
        
        canvas.restore();
    }
    
    /**
     * Adds a tab to the navigation bar
     */
    public NavigationBar addTab(int iconRes, String label) {
        if (tabs.size() >= MAX_TABS) {
            throw new IllegalStateException("Cannot add more than " + MAX_TABS + " tabs");
        }
        NavigationTab tab = new NavigationTab(iconRes, label);
        tabs.add(tab);
        createTabView(tab, tabs.size() - 1);
        return this;
    }
    
    /**
     * Adds a tab with custom NavigationTab object
     */
    public NavigationBar addTab(NavigationTab tab) {
        if (tabs.size() >= MAX_TABS) {
            throw new IllegalStateException("Cannot add more than " + MAX_TABS + " tabs");
        }
        tabs.add(tab);
        createTabView(tab, tabs.size() - 1);
        return this;
    }
    
    /**
     * Inserts a tab at specific position
     */
    public NavigationBar insertTab(int position, NavigationTab tab) {
        if (tabs.size() >= MAX_TABS) {
            throw new IllegalStateException("Cannot add more than " + MAX_TABS + " tabs");
        }
        if (position < 0 || position > tabs.size()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        tabs.add(position, tab);
        rebuildTabs();
        return this;
    }
    
    /**
     * Removes a tab at specific position
     */
    public NavigationBar removeTab(int position) {
        if (position < 0 || position >= tabs.size()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        tabs.remove(position);
        if (activeTabIndex >= tabs.size()) {
            activeTabIndex = Math.max(0, tabs.size() - 1);
        }
        rebuildTabs();
        return this;
    }
    
    /**
     * Removes all tabs
     */
    public NavigationBar clearTabs() {
        tabs.clear();
        tabContainer.removeAllViews();
        activeTabIndex = 0;
        return this;
    }
    
    /**
     * Gets the number of tabs
     */
    public int getTabCount() {
        return tabs.size();
    }
    
    /**
     * Gets the maximum number of tabs allowed
     */
    public int getMaxTabs() {
        return MAX_TABS;
    }
    
    /**
     * Gets tab at specific position
     */
    public NavigationTab getTab(int position) {
        if (position < 0 || position >= tabs.size()) {
            return null;
        }
        return tabs.get(position);
    }
    
    /**
     * Sets the active tab
     */
    public NavigationBar setActiveTab(int position) {
        if (position < 0 || position >= tabs.size()) {
            return this;
        }
        
        int oldIndex = activeTabIndex;
        activeTabIndex = position;
        
        // Update UI
        updateTabStates();
        
        // Notify listener
        if (listener != null && oldIndex != activeTabIndex) {
            listener.onTabSelected(activeTabIndex, tabs.get(activeTabIndex));
        }
        
        return this;
    }
    
    /**
     * Gets the active tab index
     */
    public int getActiveTabIndex() {
        return activeTabIndex;
    }
    
    /**
     * Gets the active tab
     */
    public NavigationTab getActiveTab() {
        if (activeTabIndex >= 0 && activeTabIndex < tabs.size()) {
            return tabs.get(activeTabIndex);
        }
        return null;
    }
    
    /**
     * Sets a badge count on a specific tab
     */
    public NavigationBar setBadge(int position, int count) {
        if (position >= 0 && position < tabs.size()) {
            tabs.get(position).badgeCount = count;
            updateTabView(position);
        }
        return this;
    }
    
    /**
     * Gets badge count for a tab
     */
    public int getBadge(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).badgeCount;
        }
        return 0;
    }
    
    /**
     * Clears badge on a tab
     */
    public NavigationBar clearBadge(int position) {
        return setBadge(position, 0);
    }
    
    /**
     * Enables or disables a tab
     */
    public NavigationBar setTabEnabled(int position, boolean enabled) {
        if (position >= 0 && position < tabs.size()) {
            tabs.get(position).enabled = enabled;
            updateTabView(position);
        }
        return this;
    }
    
    /**
     * Checks if a tab is enabled
     */
    public boolean isTabEnabled(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).enabled;
        }
        return false;
    }
    
    /**
     * Sets a tag for a tab
     */
    public NavigationBar setTabTag(int position, Object tag) {
        if (position >= 0 && position < tabs.size()) {
            tabs.get(position).tag = tag;
        }
        return this;
    }
    
    /**
     * Gets tag for a tab
     */
    public Object getTabTag(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position).tag;
        }
        return null;
    }
    
    /**
     * Sets tab selection listener
     */
    public NavigationBar setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
        return this;
    }
    
    /**
     * Enables or disables blur effect
     */
    public NavigationBar setBlurEnabled(boolean enabled) {
        this.blurEnabled = enabled;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setLayerType(enabled ? LAYER_TYPE_HARDWARE : LAYER_TYPE_NONE, null);
        }
        invalidate();
        return this;
    }
    
    /**
     * Checks if blur is enabled
     */
    public boolean isBlurEnabled() {
        return blurEnabled;
    }
    
    /**
     * Sets background color for the navbar
     */
    public NavigationBar setNavBarBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
        return this;
    }
    
    /**
     * Sets active tab background color
     */
    public NavigationBar setActiveBackgroundColor(int color) {
        this.activeBackgroundColor = color;
        updateTabStates();
        return this;
    }
    
    /**
     * Sets inactive icon color
     */
    public NavigationBar setInactiveIconColor(int color) {
        this.inactiveIconColor = color;
        updateTabStates();
        return this;
    }
    
    /**
     * Sets active icon color
     */
    public NavigationBar setActiveIconColor(int color) {
        this.activeIconColor = color;
        updateTabStates();
        return this;
    }
    
    /**
     * Sets corner radius
     */
    public NavigationBar setCornerRadius(float radius) {
        this.cornerRadius = radius;
        if (radii != null) {
            java.util.Arrays.fill(radii, dp(radius));
        }
        invalidate();
        return this;
    }
    
    /**
     * Sets badge colors
     */
    public NavigationBar setBadgeColors(int backgroundColor, int textColor) {
        this.badgeBackgroundColor = backgroundColor;
        this.badgeTextColor = textColor;
        rebuildTabs();
        return this;
    }
    
    /**
     * Finds tab index by tag
     */
    public int findTabByTag(Object tag) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).tag != null && tabs.get(i).tag.equals(tag)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Programmatically triggers tab animation (scale effect)
     */
    public NavigationBar animateTab(int position) {
        if (position >= 0 && position < tabContainer.getChildCount()) {
            View tabView = tabContainer.getChildAt(position);
            tabView.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction(() -> {
                    tabView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
        }
        return this;
    }
    
    private void createTabView(NavigationTab tab, int position) {
        FrameLayout tabView = new FrameLayout(getContext());
        tabView.setClipChildren(false);
        tabView.setClipToPadding(false);
        
        // Icon container with background
        FrameLayout iconContainer = new FrameLayout(getContext());
        iconContainer.setId(View.generateViewId());
        
        // Icon
        ImageView icon = new ImageView(getContext());
        icon.setImageResource(tab.iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int iconPadding = getContext().getResources().getDimensionPixelSize(R.dimen.navbar_tab_icon_padding);
        icon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        
        // Center icon in container
        int iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.navbar_tab_icon_size);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        iconParams.gravity = Gravity.CENTER;
        iconContainer.addView(icon, iconParams);
        
        // Badge
        if (tab.badgeCount > 0) {
            TextView badge = createBadgeView(tab.badgeCount);
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            badgeParams.gravity = Gravity.TOP | Gravity.END;
            badgeParams.setMargins(0, dp(4), dp(4), 0);
            iconContainer.addView(badge, badgeParams);
        }
        
        tabView.addView(iconContainer);
        
        // Click listener
        tabView.setOnClickListener(v -> {
            if (tab.enabled) {
                animateTab(position);
                setActiveTab(position);
            }
        });
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int tabMargin = getContext().getResources().getDimensionPixelSize(R.dimen.navbar_tab_margin);
        params.setMargins(tabMargin, 0, tabMargin, 0);
        tabContainer.addView(tabView, params);
        
        updateTabState(position);
    }
    
    private TextView createBadgeView(int count) {
        TextView badge = new TextView(getContext());
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setTextColor(badgeTextColor);
        badge.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 
            getContext().getResources().getDimension(R.dimen.badge_text_size));
        badge.setGravity(Gravity.CENTER);
        int paddingH = getContext().getResources().getDimensionPixelSize(R.dimen.badge_padding_horizontal);
        int paddingV = getContext().getResources().getDimensionPixelSize(R.dimen.badge_padding_vertical);
        badge.setPadding(paddingH, paddingV, paddingH, paddingV);
        int minSize = getContext().getResources().getDimensionPixelSize(R.dimen.badge_min_size);
        badge.setMinWidth(minSize);
        badge.setMinHeight(minSize);
        
        // Rounded background
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(badgeBackgroundColor);
        float cornerRadius = getContext().getResources().getDimension(R.dimen.badge_corner_radius);
        bg.setCornerRadius(cornerRadius);
        badge.setBackground(bg);
        
        return badge;
    }
    
    private void updateTabStates() {
        for (int i = 0; i < tabs.size(); i++) {
            updateTabState(i);
        }
    }
    
    private void updateTabView(int position) {
        if (position >= 0 && position < tabContainer.getChildCount()) {
            FrameLayout tabView = (FrameLayout) tabContainer.getChildAt(position);
            FrameLayout iconContainer = (FrameLayout) tabView.getChildAt(0);
            
            // Update badge
            NavigationTab tab = tabs.get(position);
            if (iconContainer.getChildCount() > 1) {
                iconContainer.removeViewAt(1); // Remove old badge
            }
            
            if (tab.badgeCount > 0) {
                TextView badge = createBadgeView(tab.badgeCount);
                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                );
                badgeParams.gravity = Gravity.TOP | Gravity.END;
                int badgeMarginTop = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_top);
                int badgeMarginEnd = getContext().getResources().getDimensionPixelSize(R.dimen.badge_margin_end);
                badgeParams.setMargins(0, badgeMarginTop, badgeMarginEnd, 0);
                iconContainer.addView(badge, badgeParams);
            }
            
            updateTabState(position);
        }
    }
    
    private void updateTabState(int position) {
        if (position >= 0 && position < tabContainer.getChildCount()) {
            FrameLayout tabView = (FrameLayout) tabContainer.getChildAt(position);
            FrameLayout iconContainer = (FrameLayout) tabView.getChildAt(0);
            ImageView icon = (ImageView) iconContainer.getChildAt(0);
            NavigationTab tab = tabs.get(position);
            
            boolean isActive = position == activeTabIndex;
            
            // Update background and size
            if (isActive) {
                // Make container circular by setting fixed square dimensions
                int iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.navbar_tab_icon_size);
                ViewGroup.LayoutParams params = iconContainer.getLayoutParams();
                if (params == null) {
                    params = new FrameLayout.LayoutParams(iconSize, iconSize);
                } else {
                    params.width = iconSize;
                    params.height = iconSize;
                }
                iconContainer.setLayoutParams(params);
                
                // Circular background
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setColor(activeBackgroundColor);
                float circularRadius = getContext().getResources().getDimension(R.dimen.navbar_tab_circular_radius);
                bg.setCornerRadius(circularRadius);
                iconContainer.setBackground(bg);
                
                // Center icon
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                icon.setColorFilter(activeIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // Reset to wrap content for inactive tabs
                int iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.navbar_tab_icon_size);
                ViewGroup.LayoutParams params = iconContainer.getLayoutParams();
                if (params != null) {
                    params.width = iconSize;
                    params.height = iconSize;
                    iconContainer.setLayoutParams(params);
                }
                
                iconContainer.setBackground(null);
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                icon.setColorFilter(inactiveIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
            
            // Update enabled state
            tabView.setAlpha(tab.enabled ? 1f : 0.4f);
            tabView.setEnabled(tab.enabled);
        }
    }
    
    private void rebuildTabs() {
        tabContainer.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            createTabView(tabs.get(i), i);
        }
    }
    
    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
    
    private float dpf(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
